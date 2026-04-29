package com.alem.GIA.service;

import com.alem.GIA.DTO.*;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.OtpManager;
import com.alem.GIA.entity.TempUser;

import com.alem.GIA.exception.RoleNotFoundException;
import com.alem.GIA.exception.UserNotFoundException;

import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.model.*;

import com.alem.GIA.permission.AuthenticatedUser;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.*;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class UserService {

    private final AuthenticationManager auth;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MemberService memberService;
    private final OtpMgtRepository otpMgtRepository;
    private final TempUserRepository tempUserRepository;
    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;


    public UserService(
            AuthenticationManager auth,
            UserRepository userRepository,
            JwtService jwtService, PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            MemberService memberService,
            OtpMgtRepository otpMgtRepository,
            TempUserRepository tempUserRepository,
            JavaMailSender javaMailSender, MemberRepository memberRepository) {
        this.auth = auth;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.memberService = memberService;
        this.otpMgtRepository = otpMgtRepository;
        this.tempUserRepository = tempUserRepository;
        this.javaMailSender = javaMailSender;
        this.memberRepository = memberRepository;
    }
    public String processForgetPassword(String email) {
        ApplicationUser user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(String.format("User with email %s is not found", email)));
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000));
        userRepository.save(user);


        // construct and  send reset email
        String restLink = String.format("http://localhost:4200/reset-password?token=%s", token);
        sendRestEmail(email,restLink);
        return String.format("Reset Password email sent to %s ",email);
    }
    public boolean isResetTokenValid(String token) {
        Optional<ApplicationUser> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return false;
        }

        ApplicationUser user = userOpt.get();
        return user.getTokenExpiry() != null && user.getTokenExpiry().after(new Date());
    }

    public ResetMessage resetPassword(String token, String newPassword){
        ApplicationUser user = userRepository.findByResetToken(token)
                .orElseThrow(()-> new IllegalArgumentException(String.format("Your token %s is expired or illegal",token)));
        user.setResetToken(null);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenExpiry(null);
        userRepository.save(user);
        return new ResetMessage("Password Reset Successfully");
    }
    private void sendRestEmail(String email,String link){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your password");
        message.setText(String.format("Click the following text to rest your password %s",link));
        javaMailSender.send(message);
    }
    public UserRegistrationResponse register(UserDto dto) {
        System.out.println("Request body: " + dto);
        Optional<ApplicationUser> user = userRepository.findByUserName(dto.getUserName());
        UserRegistrationResponse response = new UserRegistrationResponse();
        if (user.isPresent()) {
            response.setMessage("User name is already taken");
            return response;
        }
        Optional<ApplicationUser> userEmail = userRepository.findByEmail(dto.getEmail());
        if (userEmail.isPresent()) {
            response.setMessage("Email is already taken");
            return response;
        }
        StringBuilder otpText = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            int otpId = random.nextInt(10);
            otpText.append(otpId);
        }



        TempUser tempUser = TempUser.builder()

                .fullName(dto.getFullName())
                .userName(dto.getUserName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        tempUser = tempUserRepository.save(tempUser);
        updateOtp(dto.getUserName(), otpText.toString(), "Registration");


        response.setUserName(tempUser.getUserName());
        response.setMessage("User temporarily registered successfully");
        response.setResult(true);
        response.setUserId(tempUser.getTempUserId());
        response.setPhone(tempUser.getPhone());
        return response;
    }

    private void updateOtp(String userName, String otpText, String otpType) {
        OtpManager otpManager = new OtpManager();
        otpManager.setOtpText(otpText);
        otpManager.setUserName(userName);
        otpManager.setOtpFor(otpType);
        otpManager.setRegAt(new Date());
        otpManager.setExpireAt((new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)));
        otpMgtRepository.save(otpManager);
    }
    @Transactional
    public ConfirmOtpDto confirmReg(OtpMgtDto otpMgtDto) {

        ConfirmOtpDto confirmOtpDto;
        boolean response = validateOtp(otpMgtDto.getUserName(), otpMgtDto.getOtpText().trim());
        if (!response) {
            confirmOtpDto = ConfirmOtpDto.builder()
                    .result("Invalid Otp text")
                    .build();
        }
        Optional<TempUser> tempUser = tempUserRepository.findByUserName(otpMgtDto.getUserName());
        if (!tempUser.isPresent()) {
            return ConfirmOtpDto.builder()
                    .result(String.format("Failed for user with user name %s is not available", otpMgtDto.getUserName()))
                    .build();
        }
        try{


            // 1. First handle the Permission (save if new)
            Permission userWrite = permissionRepository.findByPermissionName("USER:WRITE")
                    .orElseGet(() -> permissionRepository.save(new Permission("USER:WRITE")));

            // 2. Then create and save the Role
            Role userRole = roleRepository.findRoleByRoleName("USER")
                    .orElseGet(() -> {
                        Role role = new Role("USER", Set.of(userWrite));
                        Role savedRole = roleRepository.save(role);

                        // Update bidirectional relationship
                        userWrite.getRoles().add(savedRole);
                        permissionRepository.save(userWrite);
                        return savedRole;

                    });


         // 4. Finally create and save the User
            ApplicationUser appUser = new ApplicationUser();
            appUser.setUserName(tempUser.get().getUserName());
            appUser.setPassword(tempUser.get().getPassword());
            appUser.setFullName(tempUser.get().getFullName());
            appUser.setEmail(tempUser.get().getEmail());
            appUser.setPhone(tempUser.get().getPhone());
            appUser.setRoles(Set.of(userRole));

            userRepository.save(appUser);
            // Auto-link existing member if admin already created one

            memberRepository.findMemberByEmail(appUser.getEmail())
                    .ifPresent(member -> {
                        member.setUser(appUser);
                        memberRepository.save(member);
                    });


            // Delete the temp user
            tempUserRepository.delete(tempUser.get());
            confirmOtpDto = ConfirmOtpDto
                    .builder()
                    .otpText(otpMgtDto.getOtpText())
                    .userName(otpMgtDto.getUserName())
                    .result("pass")
                    .build();
            return confirmOtpDto;
        }catch(Exception e) {

            return confirmOtpDto = ConfirmOtpDto.builder()
                    .result("Registration failed due to an error").build();
        }
    }


    private boolean validateOtp(String userName, String otpText) {
        Optional<OtpManager> otpManager = otpMgtRepository.findByUserName(userName);
        boolean response = false;
        if (otpManager.isPresent()) {
            if (otpManager.get().getExpireAt().after(new Date())
                    && otpText.equals(otpManager.get().getOtpText())) {
                response = true;

            }
        }
        return response;
    }

    private Set<Permission> getAuthoritiesFromRequest(RoleDto roleDTO) {
        Role role = new Role();

        return roleDTO.getPermissions().stream().map(this::getOrCreateAuthority).collect(Collectors.toSet());
        // return roleDTO.getPermissions().stream().map(p -> getOrCreateAuthority(p)).collect(Collectors.toSet());
    }

    private Permission getOrCreateAuthority(PermissionDto authorityDto) {
        return permissionRepository
                .findPermissionByPermissionName(authorityDto.getPermissionName()).orElseGet(() -> {
                    Permission auth = new Permission();
                    auth.setPermissionName(authorityDto.getPermissionName());
                    return permissionRepository.save(auth);
                });
    }

    private Role getOrCreateRole(RoleDto roleDTO) {
        return roleRepository.findRoleByRoleName(roleDTO.getRoleName()).orElseGet(() -> {
            Role r = new Role();
            r.setRoleName(roleDTO.getRoleName());
            return roleRepository.save(r);
        });
    }

    public ApplicationUser findUserByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException(String.format("%s user is not found", userName)));
    }
    public UserModelDto getUserByUserName(String userName) {
        return userRepository.findByUserName(userName).map(user ->{
                    return new UserModelDto(user.getUserName(),user.getEmail());
                })
                .orElseThrow(() -> new RuntimeException(String.format("%s user is not found", userName)));
    }

    private ApplicationUser findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(String.format("%s user is not found", email)));
    }


//    public TokenResponse authenticate(String userName, String password) {
//        ApplicationUser appUser = findUserByUserName(userName);
//
//        if (!Boolean.TRUE.equals(appUser.getStatus())) {
//            throw new DisabledException("User account is disabled");
//        }
//
//        Set<RoleDto> rolesDto = RoleDto.from(appUser.getRoles());
//
//      /*  Set<SimpleGrantedAuthority> auths = appUser.getRoles().stream()
//                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName()))
//                .collect(Collectors.toSet());*/
//        Set<SimpleGrantedAuthority> auths =
//                appUser.getRoles().stream()
//                        .flatMap(role -> Stream.concat(
//                                Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())),
//                                role.getPermissions().stream()
//                                        .map(p -> new SimpleGrantedAuthority(p.getPermissionName()))
//                        ))
//                        .collect(Collectors.toSet());
//
//
//        Authentication authentication =
//                auth.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
//
//        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
//        ApplicationUser user = principal.getUser();
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        // Extract only permissions and role names for frontend
//        Set<String> permissionNames = appUser.getRoles().stream()
//                .flatMap(r -> r.getPermissions().stream())
//                .map(Permission::getPermissionName)
//                .collect(Collectors.toSet());
//
//        Set<String> authorityNames = authorities.stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toSet());
//
//        Token token = Token.builder()
//                //.token(jwtService.generateToken(appUser, authorities))
//                .token(jwtService.generateToken(appUser, auths))
//
//                .build();
//
//        return TokenResponse.builder()
//                .userId(appUser.getId())
//                .email(appUser.getEmail())
//                .userName(appUser.getUserName())
//                .token(token)
//                .status(true)
//                .message("Authenticated successfully")
//                .permissions(permissionNames)
//                .authorities(authorityNames)
//                .roles(rolesDto)
//                .build();
//    }
public TokenResponse authenticate(String userName, String password) {
    ApplicationUser appUser = findUserByUserName(userName);

    if (!Boolean.TRUE.equals(appUser.getStatus())) {
        throw new DisabledException("User account is disabled");
    }

    Set<RoleDto> rolesDto = RoleDto.from(appUser.getRoles());

    Set<SimpleGrantedAuthority> auths =
            appUser.getRoles().stream()
                    .flatMap(role -> Stream.concat(
                            Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())),
                            role.getPermissions().stream()
                                    .map(p -> new SimpleGrantedAuthority(p.getPermissionName()))
                    ))
                    .collect(Collectors.toSet());

    Authentication authentication =
            auth.authenticate(new UsernamePasswordAuthenticationToken(userName, password));

    AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
    ApplicationUser user = principal.getUser();
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    // Extract only permissions and role names for frontend
    Set<String> permissionNames = appUser.getRoles().stream()
            .flatMap(r -> r.getPermissions().stream())
            .map(Permission::getPermissionName)
            .collect(Collectors.toSet());

    Set<String> authorityNames = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    // ✅ Merge permission names into authorities so frontend can check them directly
    Set<String> combinedAuthorities = new HashSet<>(authorityNames);
    combinedAuthorities.addAll(permissionNames);

    Token token = Token.builder()
            .token(jwtService.generateToken(appUser, auths))
            .build();

    return TokenResponse.builder()
            .userId(appUser.getId())
            .email(appUser.getEmail())
            .userName(appUser.getUserName())
            .token(token)
            .status(true)
            .message("Authenticated successfully")
            .permissions(permissionNames)
            .authorities(combinedAuthorities)   // <-- now includes both roles and permissions
            .roles(rolesDto)
            .build();
}


    public UserDto addRoleToUser(Integer userId, Integer roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        ApplicationUser user = new ApplicationUser();
        user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            user = userRepository.save(user);
        }

        return mapToUserDto(user);

    }
    private RoleDto mapToRoleDto(Role role) {
        return new RoleDto(
                role.getId(),
                role.getRoleName(),
                role.getPermissions().stream()
                        .map(p -> new PermissionDto(p.getPermissionName()))
                        .collect(Collectors.toSet())
        );
    }

    private UserDto mapToUserDto(ApplicationUser user) {
        return new UserDto(
                user.getId(),
                user.getUserName(),
                user.getPassword(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles().stream()
                        .map(this::mapToRoleDto)
                        .collect(Collectors.toSet())
        );
    }

    @Transactional(readOnly = true)
    public Set<UserDto> getAllUsers() {
        Set<ApplicationUser> allUsers = userRepository.findAllWithRolesAndPermissions();
        Set<UserDto> userDtos = allUsers.stream().map(user -> {
            UserDto userDto = new UserDto();
            userDto.setUserId(user.getId());
            userDto.setFullName(user.getFullName());
            userDto.setUserName(user.getUserName());
            userDto.setPassword(user.getPassword());
            userDto.setEmail(user.getEmail());
            userDto.setPhone(user.getPhone());
            userDto.setStatus(user.getStatus());

            userDto.setRoles(user.getRoles().stream()
                    .map(
                            role -> {
                                RoleDto roleDto = new RoleDto();
                                roleDto.setRoleId(role.getId());
                                roleDto.setRoleName(role.getRoleName());
                                roleDto.setPermissions(role.getPermissions().stream()
                                        .map(PermissionDto::fromEntity).collect(Collectors.toSet()));
                                return roleDto;
                            })
                    .collect(Collectors.toSet()));


            return userDto;
        }).collect(Collectors.toSet());
        System.out.println("Number of users returned: " + userDtos.size());
        return userDtos;
    }
    public UserDto removeRoleFromUser(Integer userId, Integer roleId) {
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        user.getRoles().remove(role);
        user = userRepository.save(user);

        return UserDto.fromEntity(user);
    }

    // Update all roles for a user (replace existing)
    public UserDto updateUserRoles(Integer userId, Set<Integer> roleIds) {
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));

        if (roles.size() != roleIds.size()) {
            throw new RoleNotFoundException("One or more roles not found");
        }

        user.setRoles(roles);
        user = userRepository.save(user);

        return UserDto.fromEntity(user);
    }

    // Get user with roles
    public UserDto getUserWithRoles(Integer userId) {
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserDto.fromEntity(user);
    }


    public void updateUser(Integer id, UserDto userDto) {
        ApplicationUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());

        // Map RoleDTOs to actual Role entities
        Set<Role> roles = userDto.getRoles().stream()
                .map(roleDto -> roleRepository.findById(roleDto.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleDto.getRoleId())))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);
    }
@Transactional
public void updateUserRoles(Integer userId, List<Integer> roleIds) {

    ApplicationUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Role adminRole = roleRepository.findByRoleName("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not configured"));

    boolean userCurrentlyAdmin = user.getRoles().stream()
            .anyMatch(r -> r.getId().equals(adminRole.getId()));

    boolean adminRequestedAfterUpdate = roleIds.contains(adminRole.getId());

    boolean adminBeingRemoved = userCurrentlyAdmin && !adminRequestedAfterUpdate;

    if (adminBeingRemoved) {
        long otherAdmins = userRepository.countOtherAdmins(userId);

        if (otherAdmins == 0) {
            throw new IllegalStateException(
                    "Cannot remove ADMIN role from the last administrator"
            );
        }
    }

    List<Role> roles = roleRepository.findAllById(roleIds);

    if (roles.size() != roleIds.size()) {
        throw new IllegalArgumentException("One or more roles are invalid");
    }

    user.setRoles(new HashSet<>(roles));
    userRepository.save(user);
}


    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }


    public UserStatusDto updateUserStatus(Integer id, boolean status) {
        ApplicationUser user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("status before update " + user.getStatus());
        user.setStatus(status);
         userRepository.save(user);
        System.out.println("status after update " + user.getStatus());
         return new UserStatusDto(user);
    }



}

