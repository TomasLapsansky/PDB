package com.vut.fit.pdb2020.controller;

import com.vut.fit.pdb2020.database.cassandra.domain.PageCql;
import com.vut.fit.pdb2020.database.cassandra.domain.ProfileDictionaryCql;
import com.vut.fit.pdb2020.database.cassandra.domain.UserCql;
import com.vut.fit.pdb2020.database.cassandra.repository.PageRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.ProfileDictionaryRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.UserRepository;
import com.vut.fit.pdb2020.database.dto.PageDetailDto;
import com.vut.fit.pdb2020.database.dto.converter.PageDtoConverter;
import com.vut.fit.pdb2020.database.mariaDB.domain.*;
import com.vut.fit.pdb2020.database.mariaDB.repository.*;
import com.vut.fit.pdb2020.utils.FileUtility;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PageController {

    @Autowired
    UserSqlRepository userSqlRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PageSqlRepository pageSqlRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    WallSqlRepository wallSqlRepository;

    @Autowired
    UserPageSqlRepository userPageSqlRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    PhotoSqlRepository photoSqlRepository;

    @Autowired
    PageDtoConverter pageDtoConverter;

    @Autowired
    ProfileDictionaryRepository profileDictionaryRepository;

    @Autowired
    ProfileDictionarySqlRepository profileDictionarySqlRepository;

    @Autowired
    FileUtility fileUtility;

    @Transactional
    @PostMapping("/page/create")
    public String createPage(@RequestParam String email, @RequestParam String name) {

        assert email != null;

        UserSql userSql = userSqlRepository.findByEmail(email);
        assert userSql != null;

        WallSql pageWall = new WallSql();
        pageWall = wallSqlRepository.save(pageWall);

        PageSql page = new PageSql();
        page.setAdmin(userSql);
        // If name not provided generate it
        page.setName((name == null) ? userSql.getName().concat("'s page") : name);
        page.setWall(pageWall);

        page = pageSqlRepository.save(page);

        UserPageSql userPageSql = new UserPageSql();
        userPageSql.setUser(userSql);
        userPageSql.setIs_admin(true);
        userPageSql.setPage(page);

        userPageSqlRepository.save(userPageSql);

        ProfileDictionarySql profileDictionarySql = new ProfileDictionarySql();
        profileDictionarySql.setPage(page);
        profileDictionarySql.setPath(page.getProfilePath());
        profileDictionarySqlRepository.save(profileDictionarySql);

        UserCql userCql = userRepository.findByEmail(email);

        if (userCql != null) {
            PageCql pageCql = new PageCql();
            pageCql.setId(page.getId());
            pageCql.setAdmin_email(userCql.getEmail());
            pageCql.setName(page.getName());
            pageCql.setProfile_path(page.getProfilePath());

            pageCql = pageRepository.save(pageCql);

            ProfileDictionaryCql profileDictionaryCql = new ProfileDictionaryCql();
            profileDictionaryCql.setProfile_path(pageCql.getProfile_path());
            profileDictionaryCql.setPage_id(pageCql.getId());
            profileDictionaryRepository.save(profileDictionaryCql);

            List<Long> newPages = userCql.getOwned_pages();
            if (newPages == null)
                newPages = new ArrayList<>();
            newPages.add(pageCql.getId());

            userCql.setOwned_pages(newPages);
            userRepository.save(userCql);

        }

        return String.format("Created page with name %s and admin %s", page.getName(), email);

    }

    @Transactional
    @PostMapping("/page/delete")
    public String deletePage(@RequestParam Long id) {

        //TODO remove all dependencies -> posts, etc ... (when finished)

        assert id != null;

        PageCql pageCql = pageRepository.findById(id);
        if (pageCql != null) {
            UserCql userCql = userRepository.findByEmail(pageCql.getAdmin_email());
            if (userCql != null) {
                List<Long> newPages = userCql.getOwned_pages().stream().filter((pageId) -> !pageId.equals(id)).collect(Collectors.toList());
                userCql.setOwned_pages(newPages);

                ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(pageCql.getProfile_path());
                profileDictionaryRepository.delete(profileDictionaryCql);
            }
            pageRepository.delete(pageCql);
        }


        PageSql pageSql = pageSqlRepository.findById(id);
        assert pageSql != null;

        pageSql.setDeleted(true);
        pageSql.setUpdated_at(Instant.now());
        pageSqlRepository.save(pageSql);

        PhotoSql photo = pageSql.getProfilePhoto();
        if (photo != null) {
            photo.setDeleted(true);
            photo.setUpdated_at(Instant.now());
            photoSqlRepository.save(photo);
        }

        WallSql wall = pageSql.getWall();
        if (wall != null) {
            wall.setDeleted(true);
            wall.setUpdated_at(Instant.now());
            wallSqlRepository.save(wall);
        }

        ProfileDictionarySql profileDictionarySql = profileDictionarySqlRepository.findByPath(pageSql.getProfilePath());
        if (profileDictionarySql != null) {
            profileDictionarySql.setDeleted(true);
            profileDictionarySql.setUpdated_at(Instant.now());
            profileDictionarySqlRepository.save(profileDictionarySql);
        }

        UserPageSql userPageSql = userPageSqlRepository.findByPage(pageSql);
        assert userPageSql != null;

        userPageSql.setDeleted(true);
        userPageSql.setUpdated_at(Instant.now());
        userPageSqlRepository.save(userPageSql);


        return "Successfully deleted page";
    }

    @Transactional
    @PostMapping("/page/addProfilePic")
    public String addPageProfilePic(@RequestParam Long id, @RequestParam MultipartFile file) throws IOException {

        assert id != null;

        PageSql page = pageSqlRepository.findById(id);
        assert page != null;

        File dest = fileUtility.saveFile(file, page.getId(), null);

        PhotoSql photoSql = new PhotoSql();

        String filePath = fileUtility.uploadsDir.concat(dest.getName());

        photoSql.setPath(filePath);
        photoSql.setPage(page);
        photoSql = photoSqlRepository.save(photoSql);

        page.setProfilePhoto(photoSql);
        page.setUpdated_at(Instant.now());
        pageSqlRepository.save(page);

        PageCql pageCql = pageRepository.findById(id);

        if (pageCql != null) {
            pageCql.setProfile_photo_path(filePath);
            pageCql.setLast_active(Instant.now());
            pageRepository.save(pageCql);
        }

        return "Photo added";

    }

    @Transactional
    @PostMapping("/page/removeProfilePic")
    public String pageRemoveProfilePic(@RequestParam Long id) {

        assert id != null;

        PageCql pageCql = pageRepository.findById(id);

        if (pageCql != null) {
            pageCql.setProfile_photo_path(null);
            pageRepository.save(pageCql);
        }

        PageSql page = pageSqlRepository.findById(id);
        assert page != null;

        PhotoSql photoSql = page.getProfilePhoto();
        assert photoSql != null;
        page.setProfilePhoto(null);
        pageSqlRepository.save(page);

        photoSql.setDeleted(true);
        photoSql.setUpdated_at(Instant.now());
        photoSqlRepository.save(photoSql);

        return "Profile pic deleted";

    }

    @Transactional
    @GetMapping("/page/{profileSlug}")
    public PageDetailDto getPageProfile(@PathVariable("profileSlug") String profileSlug) {

        assert profileSlug != null;

        Long id;
        String profilePath = "/page/".concat(profileSlug);

        ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(profilePath);

        if (profileDictionaryCql != null) {
            id = profileDictionaryCql.getPage_id();
        }
        else {
            ProfileDictionarySql profileDictionary = profileDictionarySqlRepository.findByPath(profilePath);
            id = profileDictionary.getPage().getId();
        }

        PageDetailDto pageDetailDto = null;

        PageCql pageCql = pageRepository.findById(id);
        if (pageCql != null) {
            pageDetailDto = pageDtoConverter.cqlToDetail(pageCql);
        }
        else {

            PageSql pageSql = pageSqlRepository.findById(id);
            assert pageSql != null;

            pageCql = new PageCql();

            pageCql.setName(pageSql.getName());
            pageCql.setAdmin_email(pageSql.getAdmin().getEmail());
            pageCql.setProfile_path(pageSql.getProfilePath());
            pageCql.setProfile_photo_path(pageSql.getProfilePhotoPath());
            pageRepository.save(pageCql);

            profileDictionaryCql = new ProfileDictionaryCql();
            profileDictionaryCql.setPage_id(pageCql.getId());
            profileDictionaryCql.setProfile_path(pageCql.getProfile_path());
            profileDictionaryRepository.save(profileDictionaryCql);

            pageDetailDto = pageDtoConverter.sqlToDetail(pageSql);
        }

        return pageDetailDto;

    }

}
