package com.vut.fit.pdb2020.database.mariaDB.repository;


import com.vut.fit.pdb2020.database.mariaDB.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSqlRepository extends JpaRepository<User, Long> {
}
