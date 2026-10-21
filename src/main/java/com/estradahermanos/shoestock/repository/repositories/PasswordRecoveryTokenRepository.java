package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.PasswordRecoveryTokenCrud;
import com.estradahermanos.shoestock.repository.entities.PasswordRecoveryToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PasswordRecoveryTokenRepository
{
    private final PasswordRecoveryTokenCrud passwordRecoveryTokenCrud;

    public PasswordRecoveryToken save(PasswordRecoveryToken token)
    {
        return passwordRecoveryTokenCrud.save(token);
    }

    public List<PasswordRecoveryToken> saveAll(List<PasswordRecoveryToken> tokens)
    {
        return passwordRecoveryTokenCrud.saveAll(tokens);
    }

    public Optional<PasswordRecoveryToken> findByToken(String token)
    {
        return passwordRecoveryTokenCrud.findByToken(token);
    }

    public List<PasswordRecoveryToken> findUnusedByUsername(String username)
    {
        return passwordRecoveryTokenCrud.findByUsernameAndUsedFalse(username);
    }
}
