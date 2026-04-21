package com.alem.GIA.service;

import com.alem.GIA.DTO.MemberDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import com.alem.GIA.annotation.Auditable;
import com.alem.GIA.entity.*;
import com.alem.GIA.enumes.BillingPeriod;
import com.alem.GIA.enumes.MaritalStatus;
import com.alem.GIA.enumes.PaymentMethod;
import com.alem.GIA.enumes.Relationship;
import com.alem.GIA.exception.AccessDeniedException;

import com.alem.GIA.mapper.MemberMapper;
import com.alem.GIA.model.DeleteResponse;
import com.alem.GIA.model.MemberResponse;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import org.apache.poi.ss.usermodel.*;


@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DependentRepository dependendentRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MemberMapper memberMapper;
    private final BeneficiaryService beneficiaryService;
    private final BillingService billingService;
    private final PaymentRepository paymentRepository;



    public MemberService(MemberRepository memberRepository, UserRepository userRepository, DependentRepository dependendentRepository, AuditService auditService, PasswordEncoder passwordEncoder, RoleRepository roleRepository, MemberMapper memberMapper, BeneficiaryService beneficiaryService, BillingService billingService, PaymentRepository paymentRepository) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.dependendentRepository = dependendentRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.memberMapper = memberMapper;
        this.beneficiaryService = beneficiaryService;
        this.billingService = billingService;
        this.paymentRepository = paymentRepository;
    }



    @Transactional
    public void importMembersFromExcel(MultipartFile file) {

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Map<String, Member> memberMap = new HashMap<>();
        /*    importMembers(workbook.getSheetAt(0), memberMap);
            importDependents(workbook.getSheetAt(1), memberMap);
            importPayments(workbook.getSheetAt(2), memberMap);      // ✅ Payments sheet
            importBeneficiaries(workbook.getSheetAt(3), memberMap);*/ // ✅ Beneficiaries sheet
            Sheet membersSheet = workbook.getSheet("Members");
            Sheet dependentsSheet = workbook.getSheet("Dependents");
            Sheet paymentsSheet = workbook.getSheet("Payments");
            Sheet beneficiariesSheet = workbook.getSheet("Beneficiaries");

            if(membersSheet != null) importMembers(membersSheet, memberMap);
            if(dependentsSheet != null) importDependents(dependentsSheet, memberMap);
            if(paymentsSheet != null) importPayments(paymentsSheet, memberMap);
            if(beneficiariesSheet != null) importBeneficiaries(beneficiariesSheet, memberMap);


            // FINAL SAVE → persists dependents & beneficiaries via cascade
            memberRepository.saveAll(memberMap.values());

        } catch (Exception e) {
            throw new RuntimeException("Excel import failed: " + e.getMessage(), e);
        }
    }
    private void importMembers(Sheet sheet, Map<String, Member> memberMap) {

        for (Row row : sheet) {


            if (row.getRowNum() == 0) continue;
            String emailRaw = getCellValue(row.getCell(0));

            if (emailRaw == null || emailRaw.isBlank()) continue;

            // 🔥 NORMALIZE HERE
            String email = emailRaw.trim().toLowerCase();




          //  if (email.isEmpty()) continue;


            // Fetch from DB OR create new
           // Member member = memberRepository.findByEmail(email).orElseGet(Member::new);
            Member member = memberRepository
                    .findByEmailIgnoreCase(email)
                    .orElseGet(Member::new);

           // member.setEmail(email);
          //  email = getCellValue(row.getCell(0)).trim().toLowerCase();
            member.setFirstName(getCellValue(row.getCell(1)));
            member.setLastName(getCellValue(row.getCell(2)));
            member.setEmail(email);
            member.setGender(getCellValue(row.getCell(3)));
            member.setPhone(getCellValue(row.getCell(4)));
            member.setDateOfBirth(getLocalDate(row.getCell(5)));
            member.setDateOfReg(getLocalDate(row.getCell(6)));
            member.setMaritalStatus(MaritalStatus.valueOf(getCellValue(row.getCell(7))));

             // imageName & imageType are optional
            member.setImageName(getCellValue(row.getCell(8)));
            member.setImageType(getCellValue(row.getCell(9)));

            Address address = new Address();
            address.setStreetName(getCellValue(row.getCell(10)));
            address.setCity(getCellValue(row.getCell(11)));
            address.setState(getCellValue(row.getCell(12)));
            address.setZipCode(getCellValue(row.getCell(13)));
            member.setAddress(address);

            memberMap.put(email, member);
        }
    }

    private void importDependents(Sheet sheet, Map<String, Member> memberMap) {

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;

            String email = getCellValue(row.getCell(0));

            Member member = memberMap.get(email);

            if (member == null) continue;

            Dependent dep = new Dependent();

            dep.setFirstName(getCellValue(row.getCell(1)));
            dep.setLastName(getCellValue(row.getCell(2)));
            dep.setGender(getCellValue(row.getCell(3)));

            dep.setRelationship(
                    Relationship.valueOf(getCellValue(row.getCell(4)))
            );

            dep.setDateOfBirth(getLocalDate(row.getCell(5)));

            member.addDependent(dep);
        }
    }
    private void importBeneficiaries(Sheet sheet, Map<String, Member> memberMap) {

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;

            String email = getCellValue(row.getCell(0));

            Member member = memberMap.get(email);

            if (member == null) continue;

            Beneficiary ben = new Beneficiary();

            ben.setFirstName(getCellValue(row.getCell(1)));
            ben.setLastName(getCellValue(row.getCell(2)));

            ben.setRelationship(
                    Relationship.valueOf(getCellValue(row.getCell(3)))
            );

            ben.setPercentageShare(
                    Double.valueOf(getCellValue(row.getCell(4)))
            );

            member.addBeneficiary(ben);
        }
    }
    private void importPayments(Sheet sheet, Map<String, Member> memberMap) {

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;

            String email = getCellValue(row.getCell(0));

            Member member = memberMap.get(email);

            if (member == null) continue;

            Payment payment = new Payment();
            String amountValue = getCellValue(row.getCell(1));
         BigDecimal amount;
            // 🔹 Calculate full billing summary
            if(amountValue != null && !amountValue.isEmpty()){
                // Excel provided a payment
                amount = new BigDecimal(amountValue);
            }else{
                // Auto calculate payment
                BillingSummary summary = billingService.calculateBillingSummary(member);
                amount = summary.getSuggestedPayment();
            }
            payment.setAmount(amount);

            payment.setReason(getCellValue(row.getCell(2)));
            payment.setPaymentDate(getLocalDate(row.getCell(3)));
            payment.setBillingPeriod(BillingPeriod.valueOf(getCellValue(row.getCell(4))));
            payment.setPaymentMethod(PaymentMethod.valueOf(getCellValue(row.getCell(5))));
            payment.setCheckNumber(getCellValue(row.getCell(6)));
            payment.setCardLast4(getCellValue(row.getCell(7)));
            payment.setStatus(getCellValue(row.getCell(8)));
            payment.setInvoiceNumber(getCellValue(row.getCell(9)));
            payment.setInvoiceIssued(Boolean.parseBoolean(getCellValue(row.getCell(10))));

            member.addPayment(payment);
        }
    }
    public void uploadMemberImage(
            Integer memberId,
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


    @Transactional
    public Optional<Member> getByUserIdWithCollections(Integer userId) {
       return memberRepository.findByUserIdWithCollections(userId);
       // return memberRepository.findMemberWithBeneficiariesByMemberId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Member> getByUserId(Integer userId) {
        Optional<Member> memberOpt = memberRepository.findByUserId(userId);
        memberOpt.ifPresent(member -> {
            member.getDependents().size(); // initialize dependents
            member.getPayments().size();   // initialize payments
        });
        return memberOpt;
    }
    @Auditable(
            action = "MEMBER_SELF_REGISTER",
            entity = "Member",
            idField = "member.memberId",
            captureBefore = false,
            captureAfter = true
    )
    public MemberResponse saveMember(Member member) {

        // the email is already existed
        if (validateEmailUnique(member.getEmail())) {
            return duplicateEmailResponse(member.getEmail());
        }

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
            // user.setFullName(STR."\{member.getFirstName()} \{member.getLastName()}");
            user.setFullName(String.format("%s %s",
                    member.getFirstName(),
                    member.getLastName()));

            user.setPassword(passwordEncoder.encode("ChangeMe123!"));
            user.setStatus(true);

            Role userRole = roleRepository.findRoleByRoleName("USER")
                    .orElseThrow(() -> new RuntimeException("USER role not found"));

            user.setRoles(Set.of(userRole));

            userRepository.save(user);
        }

        member.setUser(user);
        if(member.getBeneficiaries() != null){
            beneficiaryService.validateBeneficiaryShares(member.getBeneficiaries());

            member.getBeneficiaries().forEach(b -> {
                b.setMember(member);
                if(b.getAddress()!=null){
                    b.getAddress().setBeneficiary(b);
                }
            });
        }
        // ⭐ IMPORTANT FIX
        recalculateMaritalStatus(member);
        try {
            memberRepository.save(member);
            return MemberResponse.builder()
                    .member(memberMapper.toDto(member))
                    .result(true)
                    .message("Member saved")
                    .build();
        } catch (DataIntegrityViolationException ex) {
            return duplicateEmailResponse(member.getEmail());
        }


    }

    @Auditable(action = "VIEW_MEMBER")
    public Member getMemberByEmail(String email) {
        Member m = memberRepository.findByEmail(email).orElse(null);

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
            idField = "member.memberId",
            captureBefore = false,
            captureAfter = true
    )
    public MemberResponse selfRegister(Member member, String username) {
        if (validateEmailUnique(member.getEmail())) {
            return duplicateEmailResponse(member.getEmail());
        }


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

        user.setMember(member);// 🔥 BI-DIRECTIONAL LINK
        // ⭐ IMPORTANT FIX
        recalculateMaritalStatus(member);

        try {
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

        } catch (DataIntegrityViolationException ex) {
            return duplicateEmailResponse(member.getEmail());
        }


    }

    @Transactional
    public MemberResponse addMember(Member member, MultipartFile imageFile) throws IOException {
        if (validateEmailUnique(member.getEmail())) {
            return duplicateEmailResponse(member.getEmail());
        }


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

  @Transactional(readOnly = true)
   public Optional<Member> findMemberById(Integer id) {
      return memberRepository.findMemberWithDependentsByMemberId(id);
   }
    @Transactional(readOnly = true)
    public MemberDto getMember(Integer id) {

        Member member = memberRepository
                .findMemberWithDependentsByMemberId(id)
                .orElseThrow();

        return memberMapper.toDto(member);
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


    public MemberResponse updateMember(MemberDto request) {

        Member existing = memberRepository
                .findMemberWithDependentsByMemberId(request.getMemberId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Member with id %s not found"
                                        .formatted(request.getMemberId())
                        )
                );


        Member beforeCopy = copyMember(existing);

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setGender(request.getGender());
        existing.setPhone(request.getPhone());
        existing.setMaritalStatus(request.getMaritalStatus());

        if (request.getEmail() != null &&
                !existing.getEmail().equalsIgnoreCase(request.getEmail()) &&
                memberRepository.existsByEmail(request.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member with email " + request.getEmail() + " already exists"
            );
        }

        existing.setEmail(request.getEmail());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setDateOfReg(request.getDateOfReg());
        existing.setImageName(request.getImageName());
        // ⭐ Decode Base64 photo
        if (request.getPhotoBase64() != null && !request.getPhotoBase64().isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(request.getPhotoBase64());
            existing.setPhoto(imageBytes);
        }

        // NOW recalculate after all updates
       // recalculateMaritalStatus(existing);
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
        Optional<Member> member = memberRepository.findMemberWithDependentsByMemberId(id);
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


    @Transactional(readOnly = true)
    public Page<MemberDto> searchMembers(String lastName, String phone,
                                         int offset, int pageSize,
                                         String field, Sort.Direction direction) {

        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, field));

        if (lastName == null || lastName.trim().isEmpty()) {
            lastName = null;
        } else {
            lastName = "%" + lastName.toLowerCase().trim() + "%";
        }

        if (phone == null || phone.trim().isEmpty()) {
            phone = null;
        } else {
            phone = "%" + phone.trim() + "%";
        }

        Page<Member> membersPage = memberRepository.searchMembers(lastName, phone, pageable);

        return membersPage.map(member -> {
            MemberDto dto = new MemberDto(member);

            // Force initialization while session is open
            member.getDependents().size();
            member.getPayments().size();

            return dto;
        });
    }


    public MemberDto findMemberByEmail(String email) {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Member with email %s is not found ", email)));
        return new MemberDto(member);
    }
    @Transactional
    public Member addDependentToExistingMember(Integer memberId, Dependent dependent) {
        Member member = memberRepository.findMemberWithDependentsByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException(String.format("Member with Id %d not found", memberId)));
       // dependent.setMember(member);
        member.addDependent(dependent);
        recalculateMaritalStatus(member);
        return member;

       // return memberRepository.save(member);
    }
    @Transactional
    public Member addPaymentToExistingMember(Integer memberId, Payment payment) {
        Member member = memberRepository.findMemberWithDependentsByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException(String.format("Member with Id %d not found", memberId)));
        member.getPayments().add(payment);
        return memberRepository.save(member);
    }

//    public void linkMemberIfExists(ApplicationUser user) {
//
//        memberRepository
//                .findMemberByEmail(user.getEmail())
//                .ifPresent(member -> {
//                    member.setUser(user);
//                    memberRepository.save(member);
//                });
//    }
    @Transactional
    public void linkMemberIfExists(ApplicationUser user) {

        if (user == null || user.getEmail() == null) return;

        String email = user.getEmail().trim().toLowerCase();

        memberRepository.findByEmailIgnoreCase(email)
                .ifPresent(member -> {

                    // ✅ already linked? skip
                    if (member.getUser() != null) return;

                    member.setUser(user);
                    memberRepository.save(member);
                });
    }

    private boolean validateEmailUnique(String email) {
        return memberRepository.existsByEmail(email);


    }

    private MemberResponse duplicateEmailResponse(String email) {
        return MemberResponse.builder()
                .result(false)
                .message("Member with email " + email + " already exists")
                .build();
    }
    @Transactional
    public Member addBeneficiaryToExistingMember(Integer memberId, Beneficiary beneficiary) {
        Member member = memberRepository.findMemberWithBeneficiariesByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException(String.format("Member with Id %d not found", memberId)));
        beneficiary.setMember(member);
        member.getBeneficiaries().add(beneficiary);

        return memberRepository.save(member);
    }
    private void recalculateMaritalStatus(Member member) {

        boolean hasSpouse = member.getDependents().stream()
                .anyMatch(d -> d.getRelationship() == Relationship.SPOUSE);

        boolean hasChild = member.getDependents().stream()
                .anyMatch(d -> d.getRelationship() == Relationship.CHILD);

        if (!hasSpouse && !hasChild) {
            member.setMaritalStatus(MaritalStatus.SINGLE);
        }

        else if (!hasSpouse && hasChild) {
            member.setMaritalStatus(MaritalStatus.SINGLE_WITH_CHILD);
        }

        else if (hasSpouse && !hasChild) {
            member.setMaritalStatus(MaritalStatus.MARRIED_WITHOUT_CHILD);
        }

        else {
            member.setMaritalStatus(MaritalStatus.MARRIED_WITH_CHILD);
        }
    }

    private String getCellValue(Cell cell) {

        if (cell == null) return "";

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:

                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }

                return String.valueOf((long) cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();

            default:
                return "";
        }
    }
    private LocalDate getLocalDate(Cell cell) {

        if (cell == null) return null;

        try {

            if (cell.getCellType() == CellType.NUMERIC) {

                return cell.getLocalDateTimeCellValue().toLocalDate();

            }

            if (cell.getCellType() == CellType.STRING) {

                String value = cell.getStringCellValue();

                if (value == null || value.isEmpty()) return null;

                return LocalDate.parse(value);

            }

        } catch (Exception e) {

            System.out.println("Invalid date format: " + cell);

        }

        return null;
    }
}


