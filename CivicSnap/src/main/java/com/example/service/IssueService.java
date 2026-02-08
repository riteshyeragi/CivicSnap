package com.example.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.IssueResponse;
import com.example.entity.Community;
import com.example.entity.Issue;
import com.example.entity.IssueComment;
import com.example.entity.IssueUpvote;
import com.example.repository.CommunityRepository;
import com.example.repository.IssueCommentRepository;
import com.example.repository.IssueRepository;
import com.example.repository.IssueUpvoteRepository;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueCommentRepository commentRepository;
    private final IssueUpvoteRepository upvoteRepository;
    private final CommunityRepository communityRepository;
    private final GeotagOverlayService geotagOverlayService;
    private final SupabaseService supabaseService;

    public IssueService(IssueRepository issueRepository, IssueCommentRepository commentRepository,
            IssueUpvoteRepository upvoteRepository, CommunityRepository communityRepository,
            GeotagOverlayService geotagOverlayService, SupabaseService supabaseService) {
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
        this.upvoteRepository = upvoteRepository;
        this.communityRepository = communityRepository;
        this.geotagOverlayService = geotagOverlayService;
        this.supabaseService = supabaseService;
    }

    @Transactional
    public Issue createIssue(MultipartFile image, String description, List<String> tags,
            Double latitude, Double longitude, String road, String city, String country,
            String userId, Long communityId) throws IOException, InterruptedException {

        byte[] overlayedBytes = geotagOverlayService.overlayGeotag(
                image, road, city, country, latitude, longitude, tags);

        String contentType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
        String ext = contentType.contains("png") ? "png" : "jpg";
        String fileName = UUID.randomUUID() + "." + ext;

        String imageUrl = supabaseService.uploadImage(overlayedBytes, fileName, contentType);

        Long resolvedCommunityId = communityId;
        if (resolvedCommunityId == null && city != null && !city.isBlank()) {
            resolvedCommunityId = communityRepository.findByNameIgnoreCase(city)
                    .map(Community::getId)
                    .orElseGet(() -> {
                        Community newCommunity = communityRepository.save(new Community(city, "Auto-created from " + city));
                        return newCommunity.getId();
                    });
        }

        Issue issue = new Issue();
        issue.setImageUrl(imageUrl);
        issue.setDescription(description);
        issue.setTags(tags);
        issue.setLatitude(latitude);
        issue.setLongitude(longitude);
        issue.setRoad(road);
        issue.setCity(city);
        issue.setCountry(country);
        issue.setUserId(userId);
        issue.setCommunityId(resolvedCommunityId);
        issue.setStatus("pending");
        issue.setDeliveryConfirmed(true);
        issue.setUpvoteCount(0);

        return issueRepository.save(issue);
    }

    public List<IssueResponse> getFeed(String search) {
        List<Issue> issues = search != null && !search.isBlank()
                ? issueRepository.findAllWithOptionalSearch(search)
                : issueRepository.findAllWithOptionalSearch(null);

        List<IssueResponse> result = new ArrayList<>();
        for (Issue i : issues) {
            result.add(toResponse(i));
        }
        return result;
    }

    @Transactional
    public void upvote(Long issueId, String userId) {
        if (upvoteRepository.existsByIssueIdAndUserId(issueId, userId)) {
            return;
        }
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new RuntimeException("Issue not found"));
        upvoteRepository.save(new IssueUpvote(issueId, userId));
        issue.setUpvoteCount(issue.getUpvoteCount() + 1);
        issueRepository.save(issue);
    }

    @Transactional
    public IssueResponse addComment(Long issueId, String userId, String commentText) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new RuntimeException("Issue not found"));
        IssueComment comment = new IssueComment();
        comment.setIssueId(issueId);
        comment.setUserId(userId);
        comment.setCommentText(commentText);
        commentRepository.save(comment);
        return toResponse(issue);
    }

    public List<IssueResponse.CommentDto> getComments(UUID id) {
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(id).stream()
                .map(c -> {
                    IssueResponse.CommentDto dto = new IssueResponse.CommentDto();
                    dto.setId(c.getId());
                    dto.setUserId(c.getUserId());
                    dto.setCommentText(c.getCommentText());
                    dto.setCreatedAt(c.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    public List<Issue> getIssuesByCommunity(Long communityId) {
        return issueRepository.findByCommunityIdOrderByCreatedAtDesc(communityId);
    }

    @Transactional
    public Issue updateStatus(Long issueId, String status, Long authorityCommunityId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new RuntimeException("Issue not found"));
        if (!issue.getCommunityId().equals(authorityCommunityId)) {
            throw new RuntimeException("Authority can only manage issues in assigned community");
        }
        issue.setStatus(status);
        issue.setDeliveryConfirmed("resolved".equals(status));
        return issueRepository.save(issue);
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
        r.setComments(getComments(i.getId()));
        return r;
    }
}
