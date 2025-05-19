package com.example.MediBlog.repository;

import com.example.MediBlog.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByAuteurId(Long auteurId);
}
