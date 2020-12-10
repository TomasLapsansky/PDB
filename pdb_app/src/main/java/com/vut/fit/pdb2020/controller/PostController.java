package com.vut.fit.pdb2020.controller;

import com.vut.fit.pdb2020.database.cassandra.dataTypes.Like;
import com.vut.fit.pdb2020.database.cassandra.domain.PageCql;
import com.vut.fit.pdb2020.database.cassandra.domain.PagePostCql;
import com.vut.fit.pdb2020.database.cassandra.domain.UserCql;
import com.vut.fit.pdb2020.database.cassandra.domain.UserPostCql;
import com.vut.fit.pdb2020.database.cassandra.repository.PagePostRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.PageRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.UserPostRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.UserRepository;
import com.vut.fit.pdb2020.database.dto.LikeDto;
import com.vut.fit.pdb2020.database.dto.PostDto;
import com.vut.fit.pdb2020.database.dto.PostLikeDto;
import com.vut.fit.pdb2020.database.mariaDB.domain.LikeSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.PageSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.PostSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.UserSql;
import com.vut.fit.pdb2020.database.mariaDB.repository.LikeSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.PageSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.PostSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.UserSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.service.PostLikeService;
import com.vut.fit.pdb2020.database.mariaDB.service.PostService;
import com.vut.fit.pdb2020.utils.LikeAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@RestController
public class PostController {

    @Autowired
    UserSqlRepository userSqlRepository;

    @Autowired
    PostSqlRepository postSqlRepository;

    @Autowired
    PageSqlRepository pageSqlRepository;

    @Autowired
    LikeSqlRepository likeSqlRepository;

    @Autowired
    PostService postService;

    @Autowired
    PostLikeService postLikeService;


    @Transactional
    @PostMapping("/user/post/create")
    public String createUserPost(@RequestParam String email, @RequestParam String contentType, @RequestParam String textContent) throws Exception {

        assert email != null && contentType != null;

        if (contentType.equals("text") || contentType.equals("image")) {

            assert textContent != null;

            PostDto postDto = new PostDto();
            postDto.setAuthorEmail(email);
            postDto.setContentType(contentType);
            postDto.setTextContent(textContent);

            postService.createPost(postDto);

            return "Post successfully created";
        }

        return "Post creation failed";

    }

    @Transactional
    @PostMapping("/user/post/delete")
    public String deleteUserPost(@RequestParam String email, @RequestParam String contentType, @RequestParam String createdAt) {

        assert email != null && contentType != null && createdAt != null;

        PostDto postDto = new PostDto();
        postDto.setDelete(true);
        postDto.setAuthorEmail(email);
        postDto.setContentType(contentType);
        postDto.setCreatedAt(createdAt);

        postService.deletePost(postDto);

        return "Post deleted";

    }

    @Transactional
    @PostMapping("/page/post/create")
    public String createPagePost(@RequestParam Long pageId, @RequestParam String contentType, @RequestParam String textContent) throws Exception {

        assert pageId != null && contentType != null;

        if (contentType.equals("text") || contentType.equals("image")) {

            assert textContent != null;

            PostDto postDto = new PostDto();
            postDto.setPageId(pageId);
            postDto.setContentType(contentType);
            postDto.setTextContent(textContent);

            postService.createPost(postDto);

            return "Post successfully created";
        }

        return "Post creation failed";

    }

    @Transactional
    @PostMapping("/page/post/delete")
    public String deleteUserPost(@RequestParam Long pageId, @RequestParam String contentType, @RequestParam String createdAt) {

        assert pageId != null && contentType != null && createdAt != null;

        PostDto postDto = new PostDto();
        postDto.setDelete(true);
        postDto.setPageId(pageId);
        postDto.setContentType(contentType);
        postDto.setCreatedAt(createdAt);

        postService.deletePost(postDto);

        return "Post deleted";

    }

    @Transactional
    @PostMapping("/post/like")
    public Long likePost(@RequestParam String likeGiverMail, @RequestParam Long pageId, @RequestParam String userMail, @RequestParam String contentType, @RequestParam String createdAt) throws Exception {

        assert likeGiverMail != null && contentType != null && createdAt != null;

        UserSql likeGiver;
        PostSql postSql;
        Like like = new Like();
        PostLikeDto postLikeDto = new PostLikeDto();

        if (pageId != null) {

            PageSql pageSql = pageSqlRepository.findById(pageId);
            assert pageSql != null;

            postSql = postSqlRepository.findByPageAndCreatedAt(pageSql, Instant.parse(createdAt));
            assert postSql != null;

            postLikeDto.setPostOwnerId(postSql.getPage().getId());
        }
        else if (userMail != null) {

            UserSql userSql = userSqlRepository.findByEmail(userMail);
            assert userSql != null;

            postSql = postSqlRepository.findByUserAndCreatedAt(userSql, Instant.parse(createdAt));
            assert postSql != null;

            postLikeDto.setPostOwnerEmail(postSql.getUser().getEmail());
        }
        else {
            throw new Exception();
        }

        likeGiver = userSqlRepository.findByEmail(likeGiverMail);
        assert likeGiver != null;

        LikeSql likeSql = likeSqlRepository.findByUserAndPost(likeGiver, postSql);

        if (likeSql == null) {
            likeSql = new LikeSql();
            likeSql.setPost(postSql);
            likeSql.setUser(likeGiver);
        }
        else {
            likeSql.setDeleted(true);
            likeSql.setUpdated_at(Instant.now());
            postLikeDto.setCreate(false);
        }

        likeSqlRepository.save(likeSql);

        like.setAuthorName(likeGiver.getFullName());
        like.setAuthorPictureUrl(likeGiver.getProfilePhotoPath());
        like.setAuthorProfileUrl(likeGiver.getProfilePath());
        like.setCreatedAt(Instant.now());
        like.setId(likeSql.getId());

        postLikeDto.setLike(new LikeDto(like));
        postLikeDto.setPostContentType(postSql.getContent_type());
        postLikeDto.setPostCreatedAt(postSql.getCreated_at().toString());

        postLikeService.raiseEvent(postLikeDto);

        return likeSql.getId();
    }

}
