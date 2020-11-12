package com.vut.fit.pdb2020.database.cassandra.domain;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("user")
public class UserCql {
    @PrimaryKey
    private String email;

    private String name;

    private String surname;

    private String password_hash;

    private String profile_path;

    private String profile_photo_path;

    private Instant last_active;

    private Boolean status;

    private Instant created_at;

    public UserCql() {}

    public UserCql(String email, String name, String surname, String password_hash, String profile_path, String profile_photo_path, Instant last_active, Boolean status, Instant created_at) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.password_hash = password_hash;
        this.profile_path = profile_path;
        this.profile_photo_path = profile_photo_path;
        this.last_active = last_active;
        this.status = status;
        this.created_at = created_at;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getProfile_path() {
        return profile_path;
    }

    public void setProfile_path(String profile_path) {
        this.profile_path = profile_path;
    }

    public String getProfile_photo_path() {
        return profile_photo_path;
    }

    public void setProfile_photo_path(String profile_photo_path) {
        this.profile_photo_path = profile_photo_path;
    }

    public Instant getLast_active() {
        return last_active;
    }

    public void setLast_active(Instant last_active) {
        this.last_active = last_active;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }
}
