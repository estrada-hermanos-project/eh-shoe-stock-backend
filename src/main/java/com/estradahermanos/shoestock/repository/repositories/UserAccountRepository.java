package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.UserAccountCrud;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAccountRepository
{
    private final UserAccountCrud userAccountCrud;

    public UserAccount save(UserAccount userAccount)
    {
        return userAccountCrud.save(userAccount);
    }

    public Optional<UserAccount> findByUsername(String username)
    {
        return userAccountCrud.findById(username);
    }

    public boolean existsByUsername(String username)
    {
        return userAccountCrud.existsById(username);
    }
}
