package com.vut.fit.pdb2020.controller;

import com.vut.fit.pdb2020.database.mariaDB.domain.PageSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.PhotoSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.UserSql;
import com.vut.fit.pdb2020.database.mariaDB.repository.PageSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.PhotoSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.UserSqlRepository;
import com.vut.fit.pdb2020.utils.FileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;

@RestController
public class PhotoController {

    @Autowired
    UserSqlRepository userSqlRepository;

    @Autowired
    PageSqlRepository pageSqlRepository;

    @Autowired
    PhotoSqlRepository photoSqlRepository;

    @Autowired
    ServletContext context;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    FileUtility fileUtility;

    @Transactional
    @PostMapping("/user/photo/upload")
    public String uploadUserPhoto(@RequestParam String authorEmail, @RequestParam MultipartFile file) throws IOException {

        assert authorEmail != null && !file.isEmpty();

        UserSql userSql = userSqlRepository.findByEmail(authorEmail);
        assert userSql != null;

        File dest = fileUtility.saveFile(file, null, userSql.getEmail());

        PhotoSql photoSql = new PhotoSql();

        String filePath = fileUtility.uploadsDir.concat(dest.getName());

        photoSql.setPath(filePath);
        photoSql.setUser(userSql);
        photoSqlRepository.save(photoSql);

        return filePath;

    }

    @Transactional
    @PostMapping("/page/photo/upload")
    public String uploadPagePhoto(@RequestParam Long pageId, @RequestParam MultipartFile file) throws IOException {

        assert pageId != null && !file.isEmpty();

        PageSql pageSql = pageSqlRepository.findById(pageId);
        assert pageSql != null;

        File dest = fileUtility.saveFile(file, pageSql.getId(), null);

        PhotoSql photoSql = new PhotoSql();

        String filePath = fileUtility.uploadsDir.concat(dest.getName());

        photoSql.setPath(filePath);
        photoSql.setPage(pageSql);
        photoSqlRepository.save(photoSql);

        return filePath;

    }

    @Transactional
    @PostMapping("/photo/delete")
    public String deletePhoto() {

        //TODO -> when do we delete photo ?

        return "";

    }

}
