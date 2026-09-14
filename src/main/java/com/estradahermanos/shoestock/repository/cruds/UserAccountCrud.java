package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountCrud extends JpaRepository<UserAccount, String>
{
}
