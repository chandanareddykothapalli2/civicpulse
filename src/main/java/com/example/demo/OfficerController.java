package com.example.demo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Certificate;
import com.example.demo.entity.Complaint;
import com.example.demo.repository.CertificateRepository;
import com.example.demo.repository.ComplaintRepository;

@Controller
public class OfficerController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CertificateRepository certificateRepository;


    // =========================================================
    // COMMON OFFICER DASHBOARD DATA
    // =========================================================

    private void addDashboardData(Model model) {

        List<Complaint> complaints =
                complaintRepository.findAll();

        List<Certificate> certificates =
                certificateRepository.findAll();


        // =========================
        // COMPLAINT STATISTICS
        // =========================

        long totalComplaints =
                complaints.size();

        long pendingComplaints =
                complaints.stream()
                        .filter(c ->
                                "Pending".equalsIgnoreCase(
                                        c.getStatus()))
                        .count();

        long resolvedComplaints =
                complaints.stream()
                        .filter(c ->
                                "Resolved".equalsIgnoreCase(
                                        c.getStatus()))
                        .count();

        long highPriorityComplaints =
                complaints.stream()
                        .filter(c ->
                                c.getPriority() != null
                                &&
                                "High".equalsIgnoreCase(
                                        c.getPriority()))
                        .count();


        // =========================
        // CERTIFICATE STATISTICS
        // =========================

        long totalCertificates =
                certificates.size();

        long pendingCertificates =
                certificates.stream()
                        .filter(c ->
                                "Pending".equalsIgnoreCase(
                                        c.getStatus()))
                        .count();

        long approvedCertificates =
                certificates.stream()
                        .filter(c ->
                                "Approved".equalsIgnoreCase(
                                        c.getStatus()))
                        .count();


        // =========================
        // SEND DATA TO HTML
        // =========================

        model.addAttribute(
                "complaints",
                complaints
        );

        model.addAttribute(
                "certificates",
                certificates
        );

        model.addAttribute(
                "totalComplaints",
                totalComplaints
        );

        model.addAttribute(
                "pendingComplaints",
                pendingComplaints
        );

        model.addAttribute(
                "resolvedComplaints",
                resolvedComplaints
        );

        model.addAttribute(
                "highPriorityComplaints",
                highPriorityComplaints
        );

        model.addAttribute(
                "totalCertificates",
                totalCertificates
        );

        model.addAttribute(
                "pendingCertificates",
                pendingCertificates
        );

        model.addAttribute(
                "approvedCertificates",
                approvedCertificates
        );
    }


    // =========================================================
    // OFFICER LOGIN
    // =========================================================

    @GetMapping("/officer")
    public String officerLoginPage() {

        return "officerLogin";
    }


    @PostMapping("/officerLogin")
    public String officerLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if (username.equals("officer")
                && password.equals("officer123")) {

            addDashboardData(model);

            return "officerDashboard";
        }

        model.addAttribute(
                "error",
                "Invalid Username or Password"
        );

        return "officerLogin";
    }


    // =========================================================
    // OFFICER DASHBOARD
    // =========================================================

    @GetMapping("/officerComplaints")
    public String officerComplaints(Model model) {

        addDashboardData(model);

        return "officerDashboard";
    }


    // =========================================================
    // UPDATE COMPLAINT STATUS PAGE
    // =========================================================

    @GetMapping("/updateStatus/{id}")
    public String updateStatusPage(
            @PathVariable int id,
            Model model) {

        Complaint complaint =
                complaintRepository
                        .findById(id)
                        .orElse(null);

        if (complaint == null) {

            return "redirect:/officerComplaints";
        }

        model.addAttribute(
                "complaint",
                complaint
        );

        return "updateComplaintStatus";
    }


    // =========================================================
    // SAVE COMPLAINT STATUS
    // =========================================================

    @PostMapping("/updateStatus")
    public String updateStatus(
            @RequestParam int id,
            @RequestParam String status) {

        Complaint complaint =
                complaintRepository
                        .findById(id)
                        .orElse(null);

        if (complaint != null) {

            complaint.setStatus(status);

            complaintRepository.save(
                    complaint
            );
        }

        return "redirect:/officerComplaints";
    }


    // =========================================================
    // CERTIFICATE MANAGEMENT
    // =========================================================

    @GetMapping("/officerCertificates")
    public String officerCertificates(
            Model model) {

        List<Certificate> certificates =
                certificateRepository.findAll();

        model.addAttribute(
                "certificates",
                certificates
        );

        return "officerCertificates";
    }


    // =========================================================
    // VERIFY CERTIFICATE
    // =========================================================

    @PostMapping("/officerVerifyCertificate")
    public String verifyCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository
                        .findById(id)
                        .orElse(null);

        if (certificate != null) {

            if (!"Approved".equalsIgnoreCase(
                    certificate.getStatus())) {

                certificate.setVerificationStatus(
                        "Verified"
                );

                certificateRepository.save(
                        certificate
                );
            }
        }

        return "redirect:/officerCertificates";
    }


    // =========================================================
    // APPROVE CERTIFICATE
    // =========================================================

    @PostMapping("/officerApproveCertificate")
    public String approveCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository
                        .findById(id)
                        .orElse(null);

        if (certificate != null) {

            if ("Verified".equalsIgnoreCase(
                    certificate.getVerificationStatus())
                    &&
                    !"Approved".equalsIgnoreCase(
                    certificate.getStatus())) {

                certificate.setStatus(
                        "Approved"
                );

                certificate.setIssueDate(
                        LocalDate.now()
                );

                certificateRepository.save(
                        certificate
                );
            }
        }

        return "redirect:/officerCertificates";
    }

}