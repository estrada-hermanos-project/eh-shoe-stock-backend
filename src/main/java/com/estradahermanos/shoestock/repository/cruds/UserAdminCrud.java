package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAdminCrud extends JpaRepository<UserAdmin, String>
{
}
