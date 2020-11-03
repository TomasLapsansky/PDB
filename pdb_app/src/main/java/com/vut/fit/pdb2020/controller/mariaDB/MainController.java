package com.vut.fit.pdb2020.controller.mariaDB;

import com.vut.fit.pdb2020.database.cassandra.domain.User;
import com.vut.fit.pdb2020.database.cassandra.repository.UserRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.UserSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class MainController {

    @Autowired
    UserSqlRepository repositoryMar;

    @GetMapping("/maria")
    String maria() {
        List<com.vut.fit.pdb2020.database.mariaDB.domain.User> user = repositoryMar.findAll();
        return user.get(0).getName();
    }
}
