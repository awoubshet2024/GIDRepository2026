package com.alem.GIA.permission;

//import com.alem.GIA.permission.Role;
import com.alem.GIA.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static java.util.Arrays.stream;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class ApplicationUser  {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String fullName;
    private String userName;
    private String password;
    private String email;
    private String phone;
    private Boolean status = true;
    @Column(name="reset_token")
    private String resetToken;
    @Column(name="token_expiry")
    private Date tokenExpiry;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="user_roles",
            joinColumns=@JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private Set<Role> roles = new HashSet<>();
  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  private Member member;



}
