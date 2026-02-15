package com.alem.GIA.service;

import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.annotation.Auditable;
import com.alem.GIA.entity.Dependent;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import com.alem.GIA.exception.AccessDeniedException;

import com.alem.GIA.mapper.MemberMapper;
import com.alem.GIA.model.DeleteResponse;
import com.alem.GIA.model.MemberResponse;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.DependentRepository;
import com.alem.GIA.repository.MemberRepository;
import com.alem.GIA.repository.RoleRepository;
import com.alem.GIA.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DependentRepository dependendentRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MemberMapper memberMapper;


    public MemberService(MemberRepository memberRepository, UserRepository userRepository, DependentRepository dependendentRepository, AuditService auditService, PasswordEncoder passwordEncoder, RoleRepository roleRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.dependendentRepository = dependendentRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.memberMapper = memberMapper;
    }

    public void uploadMemberImage(
            Long memberId,
            MultipartFile file,
            Authentication auth
    ) {
        System.out.println("AUTH NAME: " + auth.getName());
        auth.getAuthorities().forEach(a ->
                System.out.println("AUTHORITY: " + a.getAuthority())
        );
        Member member = memberRepository.findByIdWithUser(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));





        boolean isAdmin = auth.getAuthorities().stream().anyMatch(
                a ->
                        a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals("ADMIN:WRITE"));

        boolean isOwner =
                member.getUser() != null &&
                        member.getUser().getUserName().equals(auth.getName());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to upload this image");
        }

        try {
            member.setPhoto(file.getBytes());
            member.setImageName(file.getOriginalFilename());
            member.setImageType(file.getContentType());
            memberRepository.save(member);
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }


    public Optional<Member> getByUserId(Integer userId) {
        return memberRepository.findByUserId(userId);

    }

    public MemberResponse saveMember(Member member) {
        // Check if user account already exists
        Optional<ApplicationUser> userOpt =
                userRepository.findByEmail(member.getEmail());

        ApplicationUser user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            // Create minimal user account automatically
            user = new ApplicationUser();
            user.setUserName(member.getEmail());
            user.setEmail(member.getEmail());
            user.setFullName(STR."\{member.getFirstName()} \{member.getLastName()}");
            user.setPassword(passwordEncoder.encode("ChangeMe123!"));
            user.setStatus(true);

            Role userRole = roleRepository.findRoleByRoleName("USER")
                    .orElseThrow(() -> new RuntimeException("USER role not found"));

            user.setRoles(Set.of(userRole));

            userRepository.save(user);
        }

        member.setUser(user);

        memberRepository.save(member);
        return MemberResponse.builder()
                .member(memberMapper.toDto(member))
                .result(true)
                .message("Member saved")
                .build();

    }

    @Auditable(action = "VIEW_MEMBER")
    public Member getMemberByEmail(String email) {
        Member m = memberRepository.findByEmail(email);

        auditService.logChange(
                "VIEW_MEMBER",
                "Member",
                String.valueOf(m.getMemberId()),
                null,
                m
        );

        return m;
    }


    @Transactional
    @Auditable(
            action = "MEMBER_SELF_REGISTER",
            entity = "Member",
            idField = "memberId",
            captureBefore = false,
            captureAfter = true
    )
    public MemberResponse selfRegister(Member member, String username) {

        ApplicationUser user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(member.getEmail())) {
            throw new AccessDeniedException("You can only create your own member profile");
        }

        if (user.getMember() != null) {
            return MemberResponse.builder()
                    .member(memberMapper.toDto(user.getMember()))
                    .result(true)
                    .message("Member profile already exists")
                    .build();
        }

        member.setEmail(user.getEmail());
        member.setUser(user);
        user.setMember(member);   // 🔥 BI-DIRECTIONAL LINK
        Member savedMember = memberRepository.save(member);


        MemberDto dto = MemberDto.builder()
                .memberId(savedMember.getMemberId())
                .email(savedMember.getEmail())
                .firstName(savedMember.getFirstName())
                .lastName(savedMember.getLastName())
                .build();

        return MemberResponse.builder()
                .member(dto)
                .result(true)
                .message("Member saved successfully")
                .build();


    }

    @Transactional
    public MemberResponse addMember(Member member, MultipartFile imageFile) throws IOException {

        member.setImageName(imageFile.getOriginalFilename());
        member.setImageType(imageFile.getContentType());
        member.setPhoto(imageFile.getBytes());

        // Check if user account already exists
        Optional<ApplicationUser> userOpt =
                userRepository.findByEmail(member.getEmail());

        ApplicationUser user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            // Create minimal user account automatically
            user = new ApplicationUser();
            user.setUserName(member.getEmail());
            user.setEmail(member.getEmail());
            user.setFullName(member.getFirstName() + " " + member.getLastName());
            user.setPassword(passwordEncoder.encode("ChangeMe123!"));
            user.setStatus(true);

            Role userRole = roleRepository.findRoleByRoleName("USER")
                    .orElseThrow(() -> new RuntimeException("USER role not found"));

            user.setRoles(Set.of(userRole));

            userRepository.save(user);
        }

        member.setUser(user);

        memberRepository.save(member);

        return MemberResponse.builder()
                .member(memberMapper.toDto(member))
                .result(true)
                .message("Member saved and linked to user account")
                .build();
    }

    public MemberDto findMemberByUsername(String username) {
        Member member = memberRepository.findByUser_UserName(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Member not linked to this user"));

        return new MemberDto(member);
    }



    @Transactional
    public List<Member> getAll() {
        return memberRepository.findAll();
    }

    public UserDetails loadUserByUsername(String username) {
        ApplicationUser applicationUser = memberRepository.loadUserByUsername(username);
        return null;
    }


    public Optional<Member> findMemberById(Integer id) {
        return memberRepository.findByMemberId(id);
    }

    private Member copyMember(Member m) {
        Member copy = new Member();
        copy.setMemberId(m.getMemberId());
        copy.setFirstName(m.getFirstName());
        copy.setLastName(m.getLastName());
        copy.setGender(m.getGender());
        copy.setEmail(m.getEmail());
        copy.setDateOfBirth(m.getDateOfBirth());
        copy.setDateOfReg(m.getDateOfReg());
        return copy;
    }


    public MemberResponse updateMember(Member request) {

        Member existing = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Member with id %s not found".formatted(request.getMemberId())
                        )
                );

        Member beforeCopy = copyMember(existing);

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setGender(request.getGender());
        existing.setEmail(request.getEmail());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setDateOfReg(request.getDateOfReg());
        existing.setDependents(request.getDependents());
        existing.setPayments(request.getPayments());

        Member saved = memberRepository.save(existing);

        auditService.logChange(
                "UPDATE_MEMBER",
                "Member",
                String.valueOf(saved.getMemberId()),
                beforeCopy,
                saved
        );

        return MemberResponse.builder()
                .member(memberMapper.toDto(saved))
                .result(true)
                .message("Member updated successfully")
                .build();
    }

    @Transactional
    public DeleteResponse deleteMember(Integer id) {
        Optional<Member> member = memberRepository.findByMemberId(id);
        if (member.isPresent()) {
            // First delete all dependents
            dependendentRepository.deleteAll(member.get().getDependents());
            memberRepository.delete(member.get());
            return DeleteResponse.builder()
                    .message("Member deleted successfully")
                    .status(true)
                    .build();

        } else {
            throw new IllegalArgumentException("Member with id " + id + " not found");
        }


    }

    public List<Member> findMembersWithSorting(String field) {
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, field));
    }

    public Page<Member> findMembersWithPagination(int offset, int pageSize) {

        return memberRepository.findAll(PageRequest.of(offset, pageSize));
    }


    public Long getTotalMembers() {
        return memberRepository.count();
    }


    public Page<Member> findMembersWithPaginationAndSorting(int offset, int pageSize, String field, Sort.Direction direction) {


        // return  memberRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field)));
        return memberRepository.findAll(PageRequest.of(offset, pageSize).withSort(direction, field));

    }

    @Transactional
    public MemberDto findMemberByEmail(String email) {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Member with email %s is not found ", email)));
        return new MemberDto(member);
    }

    public Member addDependentToExistingMember(Integer memberId, Dependent dependent) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException(String.format("Member with Id %d not found", memberId)));
        member.getDependents().add(dependent);

        return memberRepository.save(member);
    }

    public Member addPaymenttToExistingMember(Integer memberId, Payment payment) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException(String.format("Member with Id %d not found", memberId)));
        member.getPayments().add(payment);
        return memberRepository.save(member);
    }
    public void linkMemberIfExists(ApplicationUser user) {

        memberRepository
                .findMemberByEmail(user.getEmail())
                .ifPresent(member -> {
                    member.setUser(user);
                    memberRepository.save(member);
                });
    }

}
