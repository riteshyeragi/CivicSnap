package com.example.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.IssueResponse;
import com.example.entity.Issue;
import com.example.service.IssueService;

@RestController
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping("/api/issues")
    public ResponseEntity<IssueResponse> createIssue(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "description", required = true) String description,
            @RequestPart(value = "tags", required = true) String tagsStr,
            @RequestPart(value = "latitude", required = true) String latitudeStr,
            @RequestPart(value = "longitude", required = true) String longitudeStr,
            @RequestPart(value = "road", required = false) String road,
            @RequestPart(value = "city", required = false) String city,
            @RequestPart(value = "country", required = false) String country,
            @RequestPart(value = "community_id", required = false) String communityIdStr) {

        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> tags = parseTags(tagsStr);
        Double latitude = parseDouble(latitudeStr);
        Double longitude = parseDouble(longitudeStr);
        Long communityId = parseLong(communityIdStr);

        try {
            Issue issue = issueService.createIssue(
                    image, description, tags, latitude, longitude, road, city, country, userId, communityId);
            return ResponseEntity.ok(toResponse(issue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/api/issues")
    public ResponseEntity<List<IssueResponse>> getFeed(@RequestParam(value = "search", required = false) String search) {
        List<IssueResponse> issues = issueService.getFeed(search);
        return ResponseEntity.ok(issues);
    }

    @PostMapping("/api/issues/{id}/upvote")
    public ResponseEntity<Void> upvote(@PathVariable Long id) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            issueService.upvote(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/issues/{id}/comment")
    public ResponseEntity<IssueResponse> addComment(@PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        String commentText = body != null ? body.get("comment_text") : null;
        if (commentText == null || commentText.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            IssueResponse updated = issueService.addComment(id, userId, commentText);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/api/issues/{id}/comments")
    public ResponseEntity<List<IssueResponse.CommentDto>> getComments(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.getComments(id));
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof String) ? (String) auth.getPrincipal() : null;
    }

    private List<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tagsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private IssueResponse toResponse(Issue i) {
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
