package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionCrud extends JpaRepository<UserSession, Integer>
{
}
