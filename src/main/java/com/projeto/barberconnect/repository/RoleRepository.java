package com.projeto.barberconnect.repository;

import com.projeto.barberconnect.entity.RefreshToken;
import com.projeto.barberconnect.entity.Role;

import com.projeto.barberconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository
        extends JpaRepository<Role, Long> {

    Optional<Role> findByName(
            String name
    );
}