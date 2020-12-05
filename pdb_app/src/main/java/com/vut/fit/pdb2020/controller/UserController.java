package com.vut.fit.pdb2020.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vut.fit.pdb2020.database.cassandra.domain.ProfileDictionaryCql;
import com.vut.fit.pdb2020.database.cassandra.repository.ProfileDictionaryRepository;
import com.vut.fit.pdb2020.database.dto.UserCreateDto;
import com.vut.fit.pdb2020.database.dto.UserDetailDto;
import com.vut.fit.pdb2020.database.dto.converter.UserDtoConverter;
import com.vut.fit.pdb2020.database.mariaDB.domain.*;
import com.vut.fit.pdb2020.database.mariaDB.repository.*;
import com.vut.fit.pdb2020.utils.FileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.vut.fit.pdb2020.database.cassandra.domain.UserCql;

import com.vut.fit.pdb2020.database.cassandra.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    UserSqlRepository repositoryMar;

    @Autowired
    WallSqlRepository wallSqlRepository;

    @Autowired
    PhotoSqlRepository photoSqlRepository;

    @Autowired
    UserRepository repositoryCass;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper jsonObjectMapper;

    @Autowired
    private UserDtoConverter userDtoConverter;

    @Autowired
    UserPageSqlRepository userPageSqlRepository;

    @Autowired
    ProfileDictionaryRepository profileDictionaryRepository;

    @Autowired
    ProfileDictionarySqlRepository profileDictionarySqlRepository;

    @Autowired
    FileUtility fileUtility;


    @Transactional
    @PostMapping("/user/create")
    public String createUser(@RequestBody String userJson) throws Exception {

        UserCreateDto userCreateDto = jsonObjectMapper.readValue(userJson, UserCreateDto.class);

        if(repositoryCass.findByEmail(userCreateDto.getEmail()) != null) {
            throw new Exception("User with this email already exists");
        }
        if(repositoryMar.findByEmail(userCreateDto.getEmail()) != null) {
            throw new Exception("User with this email already exists");
        }

        WallSql wallSql = new WallSql();
        UserSql userSql = userDtoConverter.userDtoToSql(userCreateDto);

        try {
            wallSql = wallSqlRepository.save(wallSql);
            userSql.setWall(wallSql);
            repositoryMar.save(userSql);

            ProfileDictionarySql profileDictionarySql = new ProfileDictionarySql();
            profileDictionarySql.setUser(userSql);
            profileDictionarySql.setPath(userSql.getProfilePath());
            profileDictionarySqlRepository.save(profileDictionarySql);
        }
        catch (Exception e) {
            System.out.println(e.toString());
            return e.toString();
        }

        return String.format("User with email %s persisted.", userCreateDto.getEmail());
    }

    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam String email) throws Exception {

        UserCql userCql = repositoryCass.findByEmail(email);

        if (userCql != null) {
            ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(userCql.getProfile_path());
            if (profileDictionaryCql != null)
                profileDictionaryRepository.delete(profileDictionaryCql);

            repositoryCass.delete(userCql);
        }

        UserSql userSql = repositoryMar.findByEmail(email);

        if (userSql != null) {
            ProfileDictionarySql profileDictionarySql = profileDictionarySqlRepository.findByPath(userSql.getProfilePath());
            profileDictionarySql.setDeleted(true);
            profileDictionarySqlRepository.save(profileDictionarySql);

            userSql.setDeleted(true);
            userSql.setUpdated_at(Instant.now());
            repositoryMar.save(userSql);

            return String.format("User %s successfully deleted", email);
        }

        throw new Exception("User not found in source of truth");

    }

    @PostMapping("/user/addProfilePic")
    public String addProfilePic(@RequestParam String email, @RequestParam MultipartFile file ) throws IOException {

        assert email != null;

        UserSql userSql = repositoryMar.findByEmail(email);
        assert userSql != null;

       File dest = fileUtility.saveFile(file, null, email);

        PhotoSql photoSql = new PhotoSql();

        String filePath = fileUtility.uploadsDir.concat(dest.getName());

        photoSql.setPath(filePath);
        photoSql.setUser(userSql);
        photoSql = photoSqlRepository.save(photoSql);

        userSql.setProfilePhoto(photoSql);
        userSql.setUpdated_at(Instant.now());
        repositoryMar.save(userSql);

        UserCql userCql = repositoryCass.findByEmail(email);

        if (userCql != null) {
            userCql.setProfile_photo_path(filePath);
            userCql.setLast_active(Instant.now());
            repositoryCass.save(userCql);
        }

        return "Photo added";
    }

    @PostMapping("/user/removeProfilePic")
    public String removeProfilePic(@RequestParam String email) {

        assert email != null;

        UserCql userCql = repositoryCass.findByEmail(email);

        if (userCql != null) {
            userCql.setProfile_photo_path(null);
            repositoryCass.save(userCql);
        }

        UserSql userSql = repositoryMar.findByEmail(email);
        assert userSql != null;

        PhotoSql photoSql = userSql.getProfilePhoto();
        assert photoSql != null;
        userSql.setProfilePhoto(null);
        repositoryMar.save(userSql);

        photoSql.setDeleted(true);
        photoSql.setUpdated_at(Instant.now());
        photoSqlRepository.save(photoSql);

        return "Profile pic deleted";

    }

    protected void createProfileDictionaryCql(UserCql userCql) {

        ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(userCql.getProfile_path());
        if (profileDictionaryCql == null) {
            profileDictionaryCql = new ProfileDictionaryCql();
            profileDictionaryCql.setProfile_path(userCql.getProfile_path());
            profileDictionaryCql.setUser_email(userCql.getEmail());
            profileDictionaryRepository.save(profileDictionaryCql);
        }

    }

    @PostMapping("/user/login")
    public String userLogin(@RequestParam String email, @RequestParam String password) throws Exception {

        UserCql userCql = repositoryCass.findByEmail(email);

        if( userCql != null) {
            if (!passwordEncoder.matches(password, userCql.getPassword_hash()))
                throw new Exception();

            userCql.setLast_active(Instant.now());
            userCql.setStatus(true);

            this.createProfileDictionaryCql(userCql);

            try {
                repositoryCass.save(userCql);
            }
            catch (Exception e) {
                return e.toString();
            }

            return String.format("Logged in as %s", email);
        }
        else {
            UserSql userSql = repositoryMar.findByEmail(email);
            if( userSql != null) {
                if (!passwordEncoder.matches(password, userSql.getPassword_hash()))
                    throw new Exception();

                List<UserPageSql> ownedPages = userPageSqlRepository.findUserPageSqlByUser(userSql);
                List<Long> ownedPagesIds = ownedPages.stream().map((item) -> item.getPage().getId()).collect(Collectors.toList());

                userCql = new UserCql(
                        userSql.getEmail(),
                        userSql.getName(),
                        userSql.getSurname(),
                        userSql.getPassword_hash(),
                        userSql.getProfilePath(),
                        userSql.getProfilePhotoPath(),
                        Instant.now(),
                        ownedPagesIds,
                        true,
                        Instant.now()
                );

                this.createProfileDictionaryCql(userCql);

                try {
                    repositoryCass.save(userCql);
                }
                catch (Exception e) {
                    return e.toString();
                }

                return String.format("Logged in as %s", email);
            }
        }

        throw new Exception("User not found!");
    }

    @PostMapping("/user/logout")
    public String userLogout(@RequestParam String email) {

        UserCql userCql = repositoryCass.findByEmail(email);

        if (userCql.getStatus()) {
            userCql.setStatus(false);
            userCql.setLast_active(Instant.now());
            repositoryCass.save(userCql);
        }

        return "Logged out";
    }

    @Transactional
    @GetMapping("/user/{profileSlug}")
    public UserDetailDto getUserProfile(@PathVariable String profileSlug) {

        assert profileSlug != null;

        String email;

        //TODO -> might create service for this ?
        String profilePath = "/user/".concat(profileSlug);
        ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(profilePath);

        if (profileDictionaryCql != null) {
            email = profileDictionaryCql.getUser_email();
        }
        else {
            ProfileDictionarySql profileDictionary = profileDictionarySqlRepository.findByPath(profilePath);
            email = profileDictionary.getUser().getEmail();
        }

        UserDetailDto userDetailDto = null;
        UserCql userCql = repositoryCass.findByEmail(email);

        if (userCql != null) {
            userDetailDto = userDtoConverter.userCqlToDetail(userCql);


        }else {
            UserSql userSql = repositoryMar.findByEmail(email);
            assert userSql != null;

            userCql = new UserCql();

            List<UserPageSql> ownedPages = userPageSqlRepository.findUserPageSqlByUser(userSql);
            List<Long> ownedPagesIds = ownedPages.stream().map((item) -> item.getPage().getId()).collect(Collectors.toList());

            userCql.setName(userSql.getName());
            userCql.setSurname(userSql.getSurname());
            userCql.setEmail(userSql.getEmail());
            userCql.setProfile_photo_path(userCql.getProfile_photo_path());
            userCql.setProfile_path(userSql.getProfilePath());
            userCql.setPassword_hash(userSql.getPassword_hash());
            userCql.setOwned_pages(ownedPagesIds);
            userCql.setStatus(false);
            userCql.setCreated_at(Instant.now());

            userDetailDto = userDtoConverter.userSqlToDetail(userSql);
        }

        return userDetailDto;

    }

}
