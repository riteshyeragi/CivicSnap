package com.example.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Community;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByNameIgnoreCase(String name);
}
