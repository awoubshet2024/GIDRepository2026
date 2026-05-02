package com.alem.GIA.controller;

import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.annotation.Auditable;
import com.alem.GIA.entity.*;


import com.alem.GIA.mapper.MemberMapper;
import com.alem.GIA.model.*;
import com.alem.GIA.permission.ApplicationUser;

import com.alem.GIA.service.MemberService;
import com.alem.GIA.service.UserService;


import org.apache.poi.ss.usermodel.Cell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.sql.JDBCType.BOOLEAN;
import static java.sql.JDBCType.NUMERIC;
import static javax.management.openmbean.SimpleType.STRING;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final UserService userService;
    private final MemberMapper memberMapper;

    public MemberController(MemberService memberService, UserService userService, MemberMapper memberMapper) {
        this.memberService = memberService;
        this.userService = userService;
        this.memberMapper = memberMapper;
    }

    @PostMapping("/import-members")
    public ResponseEntity<Map<String,String>> importMembers(@RequestParam("file") MultipartFile file) {

        memberService.importMembersFromExcel(file);

        Map<String,String> response = new HashMap<>();
        response.put("message", "Members imported successfully");

        return ResponseEntity.ok(response);
    }
//    @PostMapping("/import-members")
//    public ResponseEntity<Map<String, Object>> importMembers(@RequestParam("file") MultipartFile file) {
//
//       // Map<String, Object> result = memberService.importMembersFromExcel(file);
//        Map<String, Object> result = memberService.importAllFromExcel(file);
//
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/{memberId}/dependents")
    public ResponseEntity<MemberDto> addDependent(@PathVariable Integer memberId, @RequestBody Dependent dependent){
        Member member = memberService.addDependentToExistingMember(memberId,dependent);

        return ResponseEntity.ok(new MemberDto(member));
    }
    @PostMapping("/{memberId}/beneficiaries")
    public ResponseEntity<Member> addBeneficiary(@PathVariable Integer memberId, @RequestBody Beneficiary beneficiary){
        Member member = memberService.addBeneficiaryToExistingMember(memberId,beneficiary);
        return ResponseEntity.ok(member);
    }
    @PostMapping("/{memberId}/payments")
    public ResponseEntity<Member> addPayment(@PathVariable Integer memberId, @RequestBody Payment payment){
        Member member = memberService.addPaymentToExistingMember(memberId,payment);
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
            @PathVariable Integer memberId,
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
    ApplicationUser current = userService.findUserByUserName(username);

    // Auto-link admin pre-created member
    memberService.linkMemberIfExists(current);

    Optional<Member> member = memberService.getByUserIdWithCollections(current.getId());

    return member
            .map(m -> ResponseEntity.ok(new MemberDto(m)))
            .orElse(ResponseEntity.notFound().build());
}






    @GetMapping("/totalRecords")
    public Long getTotalMembers(){
        return memberService.getTotalMembers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDto> getMember(@PathVariable Integer id) {
       //Member member = memberService.getMember(id)
               //.orElseThrow(() -> new RuntimeException("Member not found"));
        return ResponseEntity.ok(memberService.getMember(id));

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
    public MemberResponse updateMember(@RequestBody MemberDto memberDto) {
      return   memberService.updateMember(memberDto);

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
            @RequestParam Sort.Direction direction,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone) {

        Page<MemberDto> dtoPage =
                memberService.searchMembers(lastName, phone, offset, pageSize, field, direction);

        IPaginationParam<Page<MemberDto>> response =
                new IPaginationParam<>(dtoPage.getTotalElements(), dtoPage);

        return ResponseEntity.ok(response);
    }





}
