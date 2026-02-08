package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "authority_users")
public class AuthorityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "unique_code", nullable = false, unique = true)
    private String uniqueCode;

    @Column(name = "assigned_community_id", nullable = false)
    private Long assignedCommunityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_community_id", insertable = false, updatable = false)
    private Community assignedCommunity;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public AuthorityUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public Long getAssignedCommunityId() {
        return assignedCommunityId;
    }

    public void setAssignedCommunityId(Long assignedCommunityId) {
        this.assignedCommunityId = assignedCommunityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
