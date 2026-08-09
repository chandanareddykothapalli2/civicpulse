package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, Integer> {

    List<Certificate> findByApplicantNameContainingIgnoreCase(String applicantName);

    List<Certificate> findByStatus(String status);

    List<Certificate> findByVerificationStatus(String verificationStatus);

    long countByStatus(String status);

}