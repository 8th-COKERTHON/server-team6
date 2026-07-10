package com.team6.server.member;
import com.team6.server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Entity @Table(name="members") @NoArgsConstructor(access=AccessLevel.PROTECTED) public class Member extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=190) private String email;
 @Column(nullable=false) private String password;
 @Column(nullable=false,length=50) private String name;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Role role;
 public Member(String email,String password,String name){this.email=email;this.password=password;this.name=name;this.role=Role.USER;}
 public enum Role { USER,ADMIN }
}
