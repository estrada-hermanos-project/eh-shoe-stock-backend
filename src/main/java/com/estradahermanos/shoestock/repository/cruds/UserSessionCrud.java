package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionCrud extends JpaRepository<UserSession, Integer>
{
    Optional<UserSession> findBySessionCode(String sessionCode);
}
