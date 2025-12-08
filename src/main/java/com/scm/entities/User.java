package com.scm.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.scm.enums.Providers;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class User implements UserDetails {

    @Id
    private String userId;
    @Column(name="user_name",nullable = false)
    private String name;
    @Column(unique = true,nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(columnDefinition = "TEXT")
    private String about;
    @Column(columnDefinition = "TEXT")
    private String profilePic;
    private String phoneNumber;

    //is verified 
    private boolean enabled=false;
    private boolean emailVerified=false;
    private boolean phoneVerified=false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    //SELF, GOOGLE, GITHUB, LINKEDIN, TWITTER, FACEBOOK
    private Providers provider=Providers.SELF;
    private String providerId;

    @ToString.Exclude
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = true)
    List<Contact> contacts=new ArrayList<>();

    @ToString.Exclude
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles=new ArrayList<>();

    //Implementing Userdetails Abstract Methods 
    //for this application email is username
    @Override
    public String getUsername(){
        return this.email;
    }

    @Override
    public String getPassword(){
        return this.password;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
       List<SimpleGrantedAuthority> roleList= roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
       return roleList;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }


}
