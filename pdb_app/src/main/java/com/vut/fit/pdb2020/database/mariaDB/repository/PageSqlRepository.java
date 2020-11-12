package com.vut.fit.pdb2020.database.mariaDB.repository;

import com.vut.fit.pdb2020.database.mariaDB.domain.PageSql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageSqlRepository extends JpaRepository<PageSql, String> {

    PageSql findById(Long id);

    PageSql findByAdminId(Integer id);
}
