package com.vut.fit.pdb2020.controller.cassandra;

import com.vut.fit.pdb2020.database.cassandra.domain.UserCql;
import com.vut.fit.pdb2020.database.cassandra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class NoSqlController {

    @Autowired
    UserRepository repositoryCass;

    @GetMapping("/cassandra")
    int cassandra() {
        List<UserCql> user = repositoryCass.findAll();
        Instant time = user.get(0).getCreated_at();
        return time.getNano();
    }

}
