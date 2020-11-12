package com.vut.fit.pdb2020.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vut.fit.pdb2020.database.dto.UserCreateDto;
import com.vut.fit.pdb2020.database.dto.UserDetailDto;
import com.vut.fit.pdb2020.database.dto.converter.UserDtoConverter;
import com.vut.fit.pdb2020.database.mariaDB.domain.PhotoSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.UserPageSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.UserSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.WallSql;
import com.vut.fit.pdb2020.database.mariaDB.repository.PhotoSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.UserPageSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.WallSqlRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.vut.fit.pdb2020.database.cassandra.domain.UserCql;

import com.vut.fit.pdb2020.database.mariaDB.repository.UserSqlRepository;
import com.vut.fit.pdb2020.database.cassandra.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
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
    private ServletContext context;


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
            repositoryCass.delete(userCql);
        }

        UserSql userSql = repositoryMar.findByEmail(email);

        if (userSql != null) {
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

        String uploadsDir = "/uploads/";
        String realPathtoUploads = context.getRealPath(uploadsDir);

        if (!new File(realPathtoUploads).exists()) {
            new File(realPathtoUploads).mkdir();
        }

        // generate unique new name for file using users email and current time
        String randomFilename = passwordEncoder.encode(userSql.getEmail().concat(Instant.now().toString()));
        // remove non-alphabetical characters and take last 15 characters as filename
        String orgName = StringUtils.right(randomFilename.replaceAll("[^a-zA-Z]", ""), 15);
        String filePath = realPathtoUploads + orgName;
        File dest = new File(filePath);
        file.transferTo(dest);

        PhotoSql photoSql = new PhotoSql();

        photoSql.setPath(uploadsDir.concat(orgName));
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

    @PostMapping("/user/login")
    public String userLogin(@RequestParam String email, @RequestParam String password) throws Exception {

        UserCql userCql = repositoryCass.findByEmail(email);

        if( userCql != null) {
            if (!passwordEncoder.matches(password, userCql.getPassword_hash()))
                throw new Exception();

            userCql.setLast_active(Instant.now());
            userCql.setStatus(true);

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
                        userSql.getProfilePhoto().getPath(),
                        Instant.now(),
                        ownedPagesIds,
                        true,
                        Instant.now()
                );

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

    @GetMapping("/user")
    public UserDetailDto getUser(@RequestParam String email) throws Exception {

        assert email != null;

        UserCql userCql = repositoryCass.findByEmail(email);

        UserDetailDto userDetailDto = null;

        if (userCql == null) {
            UserSql userSql = repositoryMar.findByEmail(email);
            if (userSql == null)
                throw new Exception("User not found in source of truth");

            userDetailDto = userDtoConverter.userSqlToDetail(userSql);

        }else {
            userDetailDto = userDtoConverter.userCqlToDetail(userCql);
        }

        return userDetailDto;

    }

}
