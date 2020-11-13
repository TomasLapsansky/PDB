package com.vut.fit.pdb2020.database.dto.converter;

import com.vut.fit.pdb2020.database.cassandra.domain.PageCql;
import com.vut.fit.pdb2020.database.dto.PageDetailDto;
import com.vut.fit.pdb2020.database.mariaDB.domain.PageSql;
import org.springframework.stereotype.Component;

@Component
public class PageDtoConverter {

    public PageDetailDto cqlToDetail(PageCql page) {

        PageDetailDto pageDetailDto = null;

        if (page != null) {
            pageDetailDto = new PageDetailDto(
                    page.getName(),
                    page.getAdmin_email(),
                    page.getProfile_photo_path()
            );
        }

        return  pageDetailDto;

    }

    public PageDetailDto sqlToDetail(PageSql page) {

        PageDetailDto pageDetailDto = null;

        if (page != null) {
            pageDetailDto = new PageDetailDto(
                    page.getName(),
                    page.getAdmin().getEmail(),
                    page.getProfilePhotoPath()
            );
        }

        return  pageDetailDto;

    }

}
