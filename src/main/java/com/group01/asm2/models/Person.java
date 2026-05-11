package com.group01.asm2.models;

import com.group01.asm2.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Person {
    private Integer id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;
    private String username;
    private String password;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Person() {
    }

    public Person(Integer id, String fullName, LocalDate dateOfBirth, String email, String phone,
                  String username, String password, UserRole role,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isBuyer() {
        return role == UserRole.BUYER;
    }

    public boolean isSeller() {
        return role == UserRole.SELLER;
    }

    public boolean isRegisteredUser() {
        return role == UserRole.BUYER || role == UserRole.SELLER;
    }

    public boolean isAuctionAdministrator() {
        return role == UserRole.AUCTION_ADMINISTRATOR;
    }

    public boolean isSystemAdministrator() {
        return role == UserRole.SYSTEM_ADMINISTRATOR;
    }

    public boolean isAdministrator() {
        return role == UserRole.AUCTION_ADMINISTRATOR || role == UserRole.SYSTEM_ADMINISTRATOR;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setpassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}