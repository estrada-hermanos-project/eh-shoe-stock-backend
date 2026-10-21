package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserAdminCrud extends JpaRepository<UserAdmin, String>
{
    @Query("""
           SELECT admin FROM UserAdmin admin
            WHERE admin.email IS NOT NULL
              AND TRIM(admin.email) <> ''
           """)
    List<UserAdmin> findWithEmail();
}
