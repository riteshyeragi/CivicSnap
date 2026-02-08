package com.example.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.AuthorityUser;

public interface AuthorityUserRepository extends JpaRepository<AuthorityUser, Long> {

    Optional<AuthorityUser> findByNameAndUniqueCode(String name, String uniqueCode);
}
