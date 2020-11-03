package com.vut.fit.pdb2020.database.cassandra.domain;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.time.ZonedDateTime;

@Table("user")
public class User {
    @PrimaryKey
    private String name;

    private String surname;

    private Instant created_at;

    protected User() {}

    public String getName() {return this.name;}

    public Instant getCreated_at() {return this.created_at;}

    public User(
            String name,
            String surname,
            Instant created_at) {
        this.name = name;
        this.surname = surname;
        this.created_at = created_at;
    }

}
