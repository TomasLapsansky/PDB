package com.vut.fit.pdb2020.database.mariaDB.repository;

import com.vut.fit.pdb2020.database.mariaDB.domain.StateSql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateSqlRepository extends JpaRepository<StateSql, String> {

    StateSql findById(Integer id);

}
