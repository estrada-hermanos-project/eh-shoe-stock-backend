package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.LoginCodeCrud;
import com.estradahermanos.shoestock.repository.entities.LoginCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoginCodeRepository
{
    private final LoginCodeCrud loginCodeCrud;

    public LoginCode save(LoginCode loginCode)
    {
        return loginCodeCrud.save(loginCode);
    }

    public List<LoginCode> saveAll(List<LoginCode> loginCodes)
    {
        return loginCodeCrud.saveAll(loginCodes);
    }

    public Optional<LoginCode> findLatestByUsernameAndCode(String username, String code)
    {
        return loginCodeCrud.findFirstByUsernameAndCodeOrderByCreationDateDescIdDesc(username, code);
    }

    public List<LoginCode> findUnusedByUsername(String username)
    {
        return loginCodeCrud.findByUsernameAndUsedFalse(username);
    }
}
