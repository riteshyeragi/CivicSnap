package com.example.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.NewsDto;
import com.example.entity.News;
import com.example.repository.NewsRepository;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsRepository newsRepository;

    public NewsController(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @GetMapping
    public ResponseEntity<List<NewsDto>> getLatestNews() {
        List<News> news = newsRepository.findLatest3();
        List<NewsDto> dtos = news.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private NewsDto toDto(News n) {
        NewsDto dto = new NewsDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setDescription(n.getDescription());
        dto.setImageUrl(n.getImageUrl());
        dto.setLink(n.getLink());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
