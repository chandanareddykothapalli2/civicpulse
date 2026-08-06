package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

    // Dashboard Statistics
    long countByStatus(String status);

    long countByPriority(String priority);

    long countByDepartment(String department);

    // Search
    List<Complaint> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String title, String category);

}