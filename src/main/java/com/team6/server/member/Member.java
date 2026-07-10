package com.team6.server.member;
import com.team6.server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Entity @Table(name="members") @NoArgsConstructor(access=AccessLevel.PROTECTED) public class Member extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=190) private String email;
 @Column(nullable=false) private String password;
 @Column(nullable=false,length=50) private String name;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Role role;
 @Column(name="onboarding_completed_at") private LocalDateTime onboardingCompletedAt;
 public Member(String email,String password,String name){this(email,password,name,Role.USER);}
 public Member(String email,String password,String name,Role role){this.email=email;this.password=password;this.name=name;this.role=role;}
 public boolean isOnboardingCompleted(){return onboardingCompletedAt!=null;}
 public void completeOnboarding(LocalDateTime completedAt){if(onboardingCompletedAt==null)this.onboardingCompletedAt=completedAt;}
 public enum Role { USER,ADMIN }
}
