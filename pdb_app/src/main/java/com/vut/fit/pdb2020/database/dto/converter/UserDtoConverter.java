package com.vut.fit.pdb2020.database.dto.converter;

import com.vut.fit.pdb2020.database.cassandra.domain.UserCql;
import com.vut.fit.pdb2020.database.dto.UserCreateDto;
import com.vut.fit.pdb2020.database.dto.UserDetailDto;
import com.vut.fit.pdb2020.database.mariaDB.domain.StateSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.UserSql;
import com.vut.fit.pdb2020.database.mariaDB.repository.StateSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserDtoConverter {

    @Autowired
    private StateSqlRepository stateSqlRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserSql userDtoToSql(UserCreateDto userCreateDto) {

        UserSql userSql = null;

        if (userCreateDto != null) {
            StateSql stateSql = stateSqlRepository.findById(userCreateDto.getStateId());

            userSql = new UserSql(
                    userCreateDto.getName(),
                    userCreateDto.getSurname(),
                    userCreateDto.getEmail(),
                    passwordEncoder.encode(userCreateDto.getPassword()),
                    userCreateDto.getGender(),
                    userCreateDto.getAddress(),
                    userCreateDto.getCity(),
                    stateSql
            );
        }

        return userSql;

    }

    public UserCql userDtoToCql(UserCreateDto userCreateDto) {

        UserCql userCql = null;

        if (userCreateDto != null) {
            userCql = new UserCql();
            userCql.setEmail(userCreateDto.getEmail());
            userCql.setName(userCreateDto.getName());
            userCql.setSurname(userCreateDto.getSurname());
            userCql.setPassword_hash(passwordEncoder.encode(userCreateDto.getPassword()));
        }

        return userCql;

    }

    public UserDetailDto userSqlToDetail(UserSql userSql) {

        UserDetailDto userDetailDto = null;

        if (userSql != null) {
            userDetailDto = new UserDetailDto(
                userSql.getName(),
                userSql.getSurname(),
                userSql.getProfilePath(),
                userSql.getProfilePhotoPath(),
                false //set to false because user withdrawn from source of truth, thus inactive for more than 2w
            );
        }

        return userDetailDto;
    }

    public UserDetailDto userCqlToDetail(UserCql userCql) {

        UserDetailDto userDetailDto = null;

        if (userCql != null) {
            userDetailDto = new UserDetailDto(
                    userCql.getName(),
                    userCql.getSurname(),
                    userCql.getProfile_path(),
                    userCql.getProfile_photo_path(),
                    userCql.getStatus()
            );
        }

        return userDetailDto;
    }

}
