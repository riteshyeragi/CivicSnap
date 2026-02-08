package com.example.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.IssueUpvote;

public interface IssueUpvoteRepository extends JpaRepository<IssueUpvote, Long> {

    Optional<IssueUpvote> findByIssueIdAndUserId(Long issueId, String userId);

    boolean existsByIssueIdAndUserId(Long issueId, String userId);
}
