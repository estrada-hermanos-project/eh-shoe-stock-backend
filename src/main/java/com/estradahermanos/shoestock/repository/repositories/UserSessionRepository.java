package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.UserSessionCrud;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserSessionRepository
{
    private final UserSessionCrud userSessionCrud;

    public UserSession save(UserSession userSession)
    {
        return userSessionCrud.save(userSession);
    }
}
