package com.alem.GIA.controller;

import com.alem.GIA.DTO.*;

import com.alem.GIA.model.*;
import com.alem.GIA.otp.RegistrationService;

import com.alem.GIA.service.RoleService;
import com.alem.GIA.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("api/accounts")

@Slf4j
public class UserController {
    private final RegistrationService registrationService;
    private final UserService userService;
    private final RoleService roleService;
    private int numCall=0;


    public UserController(RegistrationService registrationService, UserService userService, RoleService roleService) {
        this.registrationService = registrationService;
        this.userService = userService;

        this.roleService = roleService;
    }
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = userService.isResetTokenValid(token);
        return ResponseEntity.ok(isValid);
    }
    @PostMapping("/forget-password")
    public ResponseEntity<Map<String,String>>processForgetPassword(@RequestBody Map<String,String> request){
        String email = request.get("email");
        String message = userService.processForgetPassword(email);
        Map<String,String>response = new HashMap<>();
        response.put("message",message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetMessage>restPassword(@RequestBody Map<String,String> request){
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        ResetMessage response = userService.resetPassword(token,newPassword);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/users")
    public Set<UserDto>getAllUserRoles(){
        System.out.println("🔁 /users called at " + LocalDateTime.now());
               return userService.getAllUsers();


    }
    @GetMapping("/user")
    public ResponseEntity<UserModelDto> findUserByUsername(@RequestParam("userName")String userName){
        return ResponseEntity.ok(userService.getUserByUserName(userName));

    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//
//        try {
//            TokenResponse response = userService.authenticate(
//                    loginRequest.getUserName(),
//                    loginRequest.getPassword()
//            );
//            return ResponseEntity.ok(response);
//        } catch (DisabledException e) {
//
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is Disabled");
//        }
//        catch (BadCredentialsException e) {
//
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
//    }
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    try {
        TokenResponse response = userService.authenticate( loginRequest.getUserName(),
                   loginRequest.getPassword());
        // Merge permissions into authorities
        if (response.getPermissions() != null) {
            response.getAuthorities().addAll(response.getPermissions());
        }
        return ResponseEntity.ok(response);
    } catch (DisabledException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is Disabled");
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}

    @PostMapping("/register")
    public UserRegistrationResponse register(@RequestBody UserDto dto) {
        return registrationService.register(dto);


    }

    @PostMapping("/confirm")
    public ConfirmOtpDto confirmOtp(@RequestBody OtpMgtDto otpMgtDto){
        return registrationService.confirmRegistration(otpMgtDto);


    }




    @PostMapping("/addRoleToUser")
    public ResponseEntity<UserDto> addRoleToUser(@RequestParam("roleId")Integer roleId, @RequestParam("userId")Integer userId){
     return ResponseEntity.ok(userService.addRoleToUser(userId,roleId));

  }
@PutMapping("/users/{userId}/roles")
public ResponseEntity<?>updateUserRoles(@PathVariable Integer userId, @RequestBody List<Integer>roleIds){
        try{
            userService.updateUserRoles(userId,roleIds);
            return ResponseEntity.ok().build();
        }catch(IllegalStateException ex){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }

}
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<?>updateRolePermissions(@PathVariable Integer roleId, @RequestBody List<Integer>permissionIds){
        roleService.updateRolePermissions(roleId,permissionIds);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        userService.updateUser(id, userDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<UserStatusDto> updateStatus(@PathVariable Integer id, @RequestParam boolean status) {
      UserStatusDto user =  userService.updateUserStatus(id,status);
        return ResponseEntity.ok(user);
    }

}
