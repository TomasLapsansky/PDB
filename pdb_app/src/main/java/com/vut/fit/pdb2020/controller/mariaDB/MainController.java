package com.vut.fit.pdb2020.controller.mariaDB;

import com.vut.fit.pdb2020.database.mariaDB.domain.UserSql;
import com.vut.fit.pdb2020.database.mariaDB.repository.UserSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MainController {

    @Autowired
    UserSqlRepository repositoryMar;

    @GetMapping("/maria")
    String maria() {
        List<UserSql> user = repositoryMar.findAll();
        return user.get(0).getName();
    }
}
