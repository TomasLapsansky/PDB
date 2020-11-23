package com.vut.fit.pdb2020.database.cassandra.domain;

import com.datastax.oss.driver.api.core.data.UdtValue;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.List;

@Table("user_post")
public class UserPostCql {

    @Column("user_email")
    private String userEmail;


    @Column("content_type")
    private String contentType;

    /* WARNING, this gives absolutely no sense... but table needs to have primary key annotated, but this
    * overrides the column name annotation thus breaks repository operations.. */
    @PrimaryKey
    private String content;

    @Column("user_profile_path")
    private String userProfilePath;

    private List<UdtValue> comments;

    private List<UdtValue> likes;

    @Column("created_at")
    private Instant createdAt;

    public String getUser_email() {
        return userEmail;
    }

    public void setUser_email(String user_email) {
        this.userEmail = user_email;
    }

    public String getContent_type() {
        return contentType;
    }

    public void setContent_type(String content_type) {
        this.contentType = content_type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser_profile_path() {
        return userProfilePath;
    }

    public void setUser_profile_path(String user_profile_path) {
        this.userProfilePath = user_profile_path;
    }

    public List<UdtValue> getComments() {
        return comments;
    }

    public void setComments(List<UdtValue> comments) {
        this.comments = comments;
    }

    public List<UdtValue> getLikes() {
        return likes;
    }

    public void setLikes(List<UdtValue> likes) {
        this.likes = likes;
    }

    public Instant getCreated_at() {
        return createdAt;
    }

    public void setCreated_at(Instant created_at) {
        this.createdAt = created_at;
    }
}
