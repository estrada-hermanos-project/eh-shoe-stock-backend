package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.PasswordRecoveryToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordRecoveryTokenCrud extends JpaRepository<PasswordRecoveryToken, Integer>
{
    Optional<PasswordRecoveryToken> findByToken(String token);

    List<PasswordRecoveryToken> findByUsernameAndUsedFalse(String username);
}
