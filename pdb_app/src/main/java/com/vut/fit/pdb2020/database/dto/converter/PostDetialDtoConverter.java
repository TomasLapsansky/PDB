package com.vut.fit.pdb2020.database.dto.converter;

import com.vut.fit.pdb2020.database.cassandra.domain.PagePostCql;
import com.vut.fit.pdb2020.database.cassandra.domain.UserPostCql;
import com.vut.fit.pdb2020.database.dto.PostDetailDto;
import com.vut.fit.pdb2020.database.mariaDB.domain.PostSql;
import org.springframework.stereotype.Component;

@Component
public class PostDetialDtoConverter {

    public PostDetailDto pagePostCqlToDto(PagePostCql post) {

        PostDetailDto postDetailDto = null;

        if ( post != null ) {
            postDetailDto = new PostDetailDto();
            postDetailDto.setContent(post.getContent());
            postDetailDto.setContentType(post.getContent_type());
            postDetailDto.setCreatedAt(post.getCreated_at());
        }

        return postDetailDto;

    }

    public PostDetailDto userPostCqlToDto(UserPostCql post) {

        PostDetailDto postDetailDto = null;

        if ( post != null ) {
            postDetailDto = new PostDetailDto();
            postDetailDto.setContent(post.getContent());
            postDetailDto.setContentType(post.getContent_type());
            postDetailDto.setCreatedAt(post.getCreated_at());
        }

        return postDetailDto;

    }

    public PostDetailDto postSqlToDto(PostSql post) {

        PostDetailDto postDetailDto = null;

        if ( post != null ) {
            postDetailDto = new PostDetailDto();
            postDetailDto.setContent(post.getContent());
            postDetailDto.setContentType(post.getContent_type());
            postDetailDto.setCreatedAt(post.getCreated_at());
        }

        return postDetailDto;

    }

}
