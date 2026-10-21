package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.LoginCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoginCodeCrud extends JpaRepository<LoginCode, Integer>
{
    Optional<LoginCode> findFirstByUsernameAndCodeOrderByCreationDateDescIdDesc(String username, String code);

    List<LoginCode> findByUsernameAndUsedFalse(String username);
}
