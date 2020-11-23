package com.vut.fit.pdb2020.database.cassandra.repository;

import com.vut.fit.pdb2020.database.cassandra.domain.UserPostCql;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.time.Instant;
import java.util.List;

public interface UserPostRepository extends CassandraRepository<UserPostCql, String> {

    List<UserPostCql> findByUserEmailAndContentTypeOrderByCreatedAt(String user_email, String content_type);

    UserPostCql findByUserEmailAndContentTypeAndCreatedAt(String user_email, String content_type, Instant created_at);

    /* TODO - this shit does not work -- ?
    @Query("delete from user_post p where p.user_email = ?1 and p.content_type = ?2 and created_at = ?3")
    void customDelete(String user_email, String content_type, Instant created_at);
    */
}
