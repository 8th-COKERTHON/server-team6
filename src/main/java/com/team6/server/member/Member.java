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
 @Enumerated(EnumType.STRING) @Column(name="onboarding_status",nullable=false,length=30) private OnboardingStatus onboardingStatus;
 @Column(name="onboarding_completed_at") private LocalDateTime onboardingCompletedAt;
 public Member(String email,String password,String name){this(email,password,name,Role.USER);}
 public Member(String email,String password,String name,Role role){this.email=email;this.password=password;this.name=name;this.role=role;this.onboardingStatus=OnboardingStatus.NOT_STARTED;}
 public boolean isOnboardingCompleted(){return onboardingStatus==OnboardingStatus.COMPLETED;}
 public void startEpisodeRegistration(){if(onboardingStatus==OnboardingStatus.NOT_STARTED)this.onboardingStatus=OnboardingStatus.EPISODE_REGISTERING;}
 public void startPlacement(){if(onboardingStatus==OnboardingStatus.NOT_STARTED||onboardingStatus==OnboardingStatus.EPISODE_REGISTERING)this.onboardingStatus=OnboardingStatus.PLACEMENT_IN_PROGRESS;}
 public void completeOnboarding(LocalDateTime completedAt){if(onboardingStatus!=OnboardingStatus.COMPLETED){this.onboardingStatus=OnboardingStatus.COMPLETED;this.onboardingCompletedAt=completedAt;}}
 public enum Role { USER,ADMIN }
 public enum OnboardingStatus { NOT_STARTED,EPISODE_REGISTERING,PLACEMENT_IN_PROGRESS,COMPLETED }
}
