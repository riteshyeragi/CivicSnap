package com.example.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.entity.News;

public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n ORDER BY n.createdAt DESC")
    List<News> findLatest3(PageRequest pageRequest);

    default List<News> findLatest3() {
        return findLatest3(PageRequest.of(0, 3));
    }
}
