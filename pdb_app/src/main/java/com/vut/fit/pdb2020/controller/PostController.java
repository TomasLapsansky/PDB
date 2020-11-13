package com.vut.fit.pdb2020.controller;

import com.vut.fit.pdb2020.database.cassandra.domain.PageCql;
import com.vut.fit.pdb2020.database.cassandra.domain.PagePostCql;
import com.vut.fit.pdb2020.database.cassandra.domain.UserCql;
import com.vut.fit.pdb2020.database.cassandra.domain.UserPostCql;
import com.vut.fit.pdb2020.database.cassandra.repository.PagePostRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.PageRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.UserPostRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.UserRepository;
import com.vut.fit.pdb2020.database.mariaDB.domain.PageSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.PostSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.UserSql;
import com.vut.fit.pdb2020.database.mariaDB.repository.PageSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.PostSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.UserSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@RestController
public class PostController {

    @Autowired
    UserSqlRepository userSqlRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostSqlRepository postSqlRepository;

    @Autowired
    UserPostRepository userPostRepository;

    @Autowired
    PagePostRepository pagePostRepository;

    @Autowired
    PageSqlRepository pageSqlRepository;

    @Autowired
    PageRepository pageRepository;


    @Transactional
    @PostMapping("/user/post/create")
    public String createUserPost(@RequestParam String email, @RequestParam String contentType, @RequestParam String textContent) {

        assert email != null && contentType != null;

        if (contentType.equals("text") || contentType.equals("image")) {

            assert textContent != null;

            UserSql userSql = userSqlRepository.findByEmail(email);
            assert userSql != null;

            PostSql postSql = new PostSql();
            postSql.setUser(userSql);
            postSql.setWall(userSql.getWall());
            postSql.setContent_type(contentType);
            postSql.setContent(textContent);
            postSql.setCreated_at(Instant.now());
            postSqlRepository.save(postSql);

            UserCql userCql = userRepository.findByEmail(email);
            assert userCql != null;

            UserPostCql userPostCql = new UserPostCql();
            userPostCql.setUser_email(userCql.getEmail());
            userPostCql.setUser_profile_path(userCql.getProfile_path());
            userPostCql.setContent(textContent);
            userPostCql.setContent_type(contentType);
            userPostCql.setCreated_at(Instant.now());
            userPostRepository.save(userPostCql);

            return "Post successfully created";
        }

        return "Post creation failed";

    }

    //TODO solve cassandra delete
    @Transactional
    @PostMapping("/user/post/delete")
    public String deleteUserPost(@RequestParam String email, @RequestParam String contentType, @RequestParam String createdAt) {

        assert email != null && contentType != null && createdAt != null;

        UserCql userCql = userRepository.findByEmail(email);

        if (userCql != null) {
            UserPostCql userPostCql = userPostRepository.findByUserEmailAndContentTypeAndCreatedAt(email, contentType, Instant.parse(createdAt));
            if (userPostCql != null) {
                userPostRepository.delete(userPostCql);
            }
        }

        UserSql userSql = userSqlRepository.findByEmail(email);
        assert userSql != null;

        PostSql postSql = postSqlRepository.findByUserAndCreatedAt(userSql, Instant.parse(createdAt));
        assert postSql != null;

        postSql.setUpdated_at(Instant.now());
        postSql.setDeleted(true);
        postSqlRepository.save(postSql);

        return "Post deleted";

    }

    @Transactional
    @PostMapping("/page/post/create")
    public String createPagePost(@RequestParam Long pageId, @RequestParam String contentType, @RequestParam String textContent) {

        assert pageId != null && contentType != null;

        if (contentType.equals("text") || contentType.equals("image")) {

            assert textContent != null;

            PageSql pageSql = pageSqlRepository.findById(pageId);
            assert pageSql != null;

            PostSql postSql = new PostSql();
            postSql.setPage(pageSql);
            postSql.setWall(pageSql.getWall());
            postSql.setContent_type(contentType);
            postSql.setContent(textContent);
            postSql.setCreated_at(Instant.now());
            postSqlRepository.save(postSql);

            PageCql pageCql = pageRepository.findById(pageId);
            assert pageCql != null;

            PagePostCql pagePostCql = new PagePostCql();
            pagePostCql.setPage_id(pageCql.getId());
            pagePostCql.setPage_name(pageCql.getName());
            pagePostCql.setContent(textContent);
            pagePostCql.setContent_type(contentType);
            pagePostCql.setCreated_at(Instant.now());
            pagePostRepository.save(pagePostCql);

            return "Post successfully created";
        }

        return "Post creation failed";

    }

    // TODO - solve cassandra delete
    @Transactional
    @PostMapping("/page/post/delete")
    public String deleteUserPost(@RequestParam Long pageId, @RequestParam String contentType, @RequestParam String createdAt) {

        assert pageId != null && contentType != null && createdAt != null;

        PageCql pageCql = pageRepository.findById(pageId);

        if (pageCql != null) {
            PagePostCql pagePostCql = pagePostRepository.findByPageIdAndContentTypeAndCreatedAt(pageId, contentType, Instant.parse(createdAt));
            if (pagePostCql != null) {
                pagePostRepository.delete(pagePostCql);
            }
        }

        PageSql pageSql = pageSqlRepository.findById(pageId);
        assert pageSql != null;

        PostSql postSql = postSqlRepository.findByPageAndCreatedAt(pageSql, Instant.parse(createdAt));
        assert postSql != null;

        postSql.setUpdated_at(Instant.now());
        postSql.setDeleted(true);
        postSqlRepository.save(postSql);

        return "Post deleted";

    }

}
