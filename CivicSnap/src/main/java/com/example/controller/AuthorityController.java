package com.example.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.AuthResponse;
import com.example.dto.IssueResponse;
import com.example.entity.Issue;
import com.example.repository.AuthorityUserRepository;
import com.example.service.AuthorityJwtService;
import com.example.service.IssueService;

@RestController
@RequestMapping("/api/authority")
public class AuthorityController {

    private final AuthorityUserRepository authorityUserRepository;
    private final AuthorityJwtService authorityJwtService;
    private final IssueService issueService;

    public AuthorityController(AuthorityUserRepository authorityUserRepository,
            AuthorityJwtService authorityJwtService, IssueService issueService) {
        this.authorityUserRepository = authorityUserRepository;
        this.authorityJwtService = authorityJwtService;
        this.issueService = issueService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String uniqueCode = body.get("unique_code");
        if (name == null || uniqueCode == null) {
            return ResponseEntity.badRequest().build();
        }
        return authorityUserRepository.findByNameAndUniqueCode(name, uniqueCode)
                .map(auth -> {
                    String token = authorityJwtService.generateToken(
                            auth.getId().toString(), auth.getAssignedCommunityId(), auth.getName());
                    return ResponseEntity.ok(new AuthResponse(token, auth.getId().toString(), auth.getName()));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/issues")
    public ResponseEntity<List<IssueResponse>> getIssues() {
        Long communityId = getAuthorityCommunityId();
        if (communityId == null) {
            return ResponseEntity.status(403).build();
        }
        List<Issue> issues = issueService.getIssuesByCommunity(communityId);
        List<IssueResponse> result = issues.stream()
                .map(i -> issueToResponse(i))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/issues/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long communityId = getAuthorityCommunityId();
        if (communityId == null) {
            return ResponseEntity.status(403).build();
        }
        String status = body.get("status");
        if (status == null || !List.of("pending", "in-progress", "resolved").contains(status)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Issue updated = issueService.updateStatus(id, status, communityId);
            return ResponseEntity.ok(issueToResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Long getAuthorityCommunityId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() != null && auth.getDetails() instanceof Long) {
            return (Long) auth.getDetails();
        }
        return null;
    }

    private IssueResponse issueToResponse(Issue i) {
        IssueResponse r = new IssueResponse();
        r.setId(i.getId());
        r.setImageUrl(i.getImageUrl());
        r.setDescription(i.getDescription());
        r.setLatitude(i.getLatitude());
        r.setLongitude(i.getLongitude());
        r.setRoad(i.getRoad());
        r.setCity(i.getCity());
        r.setCountry(i.getCountry());
        r.setCreatedAt(i.getCreatedAt());
        r.setUserId(i.getUserId());
        r.setCommunityId(i.getCommunityId());
        r.setStatus(i.getStatus());
        r.setDeliveryConfirmed(i.getDeliveryConfirmed());
        r.setTags(i.getTags());
        r.setUpvoteCount(i.getUpvoteCount());
        r.setReporter(new IssueResponse.ReporterInfo(i.getUserId(), null));
        r.setComments(issueService.getComments(i.getId()));
        return r;
    }
}
