package com.briscola4legenDs.briscola.User;

import jakarta.persistence.*;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table
@Builder
public class User implements UserDetails {
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;

    @Column(unique = true)
    private String email;
    private String username;
    private String password;

    private UserRole role = UserRole.USER;

    public User() {}

    public User(String email, String username, String password) {
        setEmail(email);
        setUsername(username);
        setPassword(password);
    }

    public User(Long id, String username, String email, String password) {
        this.id = id;
        setEmail(email);
        setUsername(username);
        setPassword(password);
    }

    public User(Long id, String email, String username, String password, UserRole role) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setUsername(String username) throws IllegalArgumentException{
        if (username == null || username.isEmpty())
            throw new IllegalArgumentException("Username cannot be null or empty");
        this.username = username;
    }

    public void setPassword(String password) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Password cannot be null or empty");
        this.password = password;
    }

    public void setEmail(String email) {
        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email cannot be null or empty");
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
        return Collections.singletonList(authority);
    }
}
