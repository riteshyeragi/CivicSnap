package com.example.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByCommunityIdOrderByCreatedAtDesc(Long communityId);

    @Query("SELECT i FROM Issue i WHERE (:search IS NULL OR :search = '' OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.road) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.country) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY i.createdAt DESC")
    List<Issue> findAllWithOptionalSearch(@Param("search") String search);
}
