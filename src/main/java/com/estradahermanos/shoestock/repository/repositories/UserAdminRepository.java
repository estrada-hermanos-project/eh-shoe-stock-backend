package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.UserAdminCrud;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAdminRepository
{
    private final UserAdminCrud userAdminCrud;

    public UserAdmin save(UserAdmin userAdmin)
    {
        return userAdminCrud.save(userAdmin);
    }

    public Optional<UserAdmin> findByDpi(String dpi)
    {
        return userAdminCrud.findById(dpi);
    }

    public List<UserAdmin> findAll()
    {
        return userAdminCrud.findAll();
    }

    public boolean existsByDpi(String dpi)
    {
        return userAdminCrud.existsById(dpi);
    }

    public void deleteByDpi(String dpi)
    {
        userAdminCrud.deleteById(dpi);
    }
}
