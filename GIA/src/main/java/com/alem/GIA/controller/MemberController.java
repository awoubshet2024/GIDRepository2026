package com.alem.GIA.controller;

import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.annotation.Auditable;
import com.alem.GIA.entity.Dependent;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import com.alem.GIA.exception.AccessDeniedException;
import com.alem.GIA.model.*;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.AuthenticatedUser;
import com.alem.GIA.service.MemberService;
import com.alem.GIA.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final UserService userService;

    public MemberController(MemberService memberService, UserService userService) {
        this.memberService = memberService;
        this.userService = userService;
    }

    @GetMapping("/check-please")
    public String checkMember(HttpServletRequest request) {
        return "member check is ok " + request.getSession().getId();

    }
    @PostMapping("/{memberId}/dependents")
    public ResponseEntity<Member> addDependent(@PathVariable Integer memberId, @RequestBody Dependent dependent){
        Member member = memberService.addDependentToExistingMember(memberId,dependent);
        return ResponseEntity.ok(member);
    }
    @PostMapping("/{memberId}/payments")
    public ResponseEntity<Member> addDependent(@PathVariable Integer memberId, @RequestBody Payment payment){
        Member member = memberService.addPaymenttToExistingMember(memberId,payment);
        return ResponseEntity.ok(member);
    }
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Integer id) {
        Member member = memberService.findMemberById(id).orElseThrow(() -> new RuntimeException("Member not found"));
        byte[]image = member.getPhoto();
        return ResponseEntity.ok().contentType(MediaType.valueOf(member.getImageType())).body(image);
    }
    @PostMapping("/{memberId}/image")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long memberId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        memberService.uploadMemberImage(memberId, file, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public MemberResponse createMember(@RequestBody Member member) {
        return memberService.saveMember(member);

    }
    @PostMapping("/self-register")
    public MemberResponse selfRegister(
            @RequestBody Member member,
            Authentication authentication
    ) {
        String loggedInUsername = authentication.getName();


        return memberService.selfRegister(member, loggedInUsername);
    }



@GetMapping("/me")
public ResponseEntity<MemberDto> getMe(Principal principal) {

    String username = principal.getName();

    ApplicationUser current =
            userService.findUserByUserName(username);

    // 🔥 Auto-link if admin pre-created membe
    memberService.linkMemberIfExists(current);


    Optional<Member> member =
            memberService.getByUserId(current.getId());

    return member
            .map(m -> ResponseEntity.ok(new MemberDto(m)))
            .orElse(ResponseEntity.notFound().build());
}






    @GetMapping("/totalRecords")
    public Long getTotalMembers(){
        return memberService.getTotalMembers();
    }
    @GetMapping("/{id}")
    public Optional<Member> getMember(@PathVariable Integer id) {
        return memberService.findMemberById(id);
    }

    @GetMapping("/all")
    public List<MemberDto>
    getAllMembers(){
        return memberService.getAll()
                .stream().map(MemberDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/member/{email}")
    @Auditable(
            action = "VIEW_MEMBER",
            entity = "Member",
            idField = "memberId",
            captureAfter = true
    )
    public ResponseEntity<MemberDto>getMemberByEmail(@PathVariable String email){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth.getAuthorities());
        MemberDto memberDto = memberService.findMemberByEmail(email);
        return ResponseEntity.ok(memberDto);
    }





    @PutMapping("/updateMember")
    public MemberResponse updateMember(@RequestBody Member member) {
      return   memberService.updateMember(member);

    }

    @DeleteMapping("/deleteMember/{id}")
    public DeleteResponse deleteMember(@PathVariable Integer id) {

        return  memberService.deleteMember(id);

    }
    @GetMapping("/sort/{field}")
    private APIResponse<List<Member>> getMembersWithSort(@PathVariable String field) {
        List<Member> allMembers = memberService.findMembersWithSorting(field);
        return new APIResponse<>(allMembers.size(), allMembers);
    }
    @GetMapping("/paginationAndSort")
    public ResponseEntity<IPaginationParam<Page<MemberDto>>> getMembers(
            @RequestParam int offset,
            @RequestParam int pageSize,
            @RequestParam String field,
            @RequestParam Sort.Direction direction) {

        Page<Member> membersPage = memberService.findMembersWithPaginationAndSorting(offset, pageSize, field, direction);

        Page<MemberDto> dtoPage = membersPage.map(MemberDto::new);

        IPaginationParam<Page<MemberDto>> response = new IPaginationParam<>(
                dtoPage.getTotalElements(), dtoPage
        );

        return ResponseEntity.ok(response);
    }




}
