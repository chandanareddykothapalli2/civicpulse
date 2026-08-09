package com.example.demo;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Certificate;
import com.example.demo.repository.CertificateRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

@Controller
public class CertificateController {

    @Autowired
    private CertificateRepository certificateRepository;

    // Open certificate application page
    @GetMapping("/certificate")
    public String certificatePage(Model model) {
        return "certificate";
    }

    // Submit certificate application
    @PostMapping("/certificate")
    public String saveCertificate(
            @RequestParam String applicantName,
            @RequestParam String certificateType,
            @RequestParam String department) {

        Certificate certificate = new Certificate();

        certificate.setApplicantName(applicantName);
        certificate.setCertificateType(certificateType);
        certificate.setDepartment(department);

        certificate.setStatus("Pending");
        certificate.setVerificationStatus("Not Verified");
        certificate.setIssueDate(null);

        // Save first so that ID is generated
        Certificate savedCertificate = certificateRepository.save(certificate);

        // Generate Application ID
        savedCertificate.setApplicationId(
                "CERT" + (1000 + savedCertificate.getId())
        );

        certificateRepository.save(savedCertificate);

        return "redirect:/certificates";
    }

    // View all certificate applications
    @GetMapping("/certificates")
    public String viewCertificates(Model model) {

        List<Certificate> certificates =
                certificateRepository.findAll();

        model.addAttribute("certificates", certificates);

        return "certificates";
    }

    // Verify certificate
    @PostMapping("/verifyCertificate")
    public String verifyCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository.findById(id).orElse(null);

        if (certificate != null) {

            certificate.setVerificationStatus("Verified");

            certificateRepository.save(certificate);
        }

        return "redirect:/certificates";
    }

    // Approve certificate
    @PostMapping("/approveCertificate")
    public String approveCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository.findById(id).orElse(null);

        if (certificate != null) {

            certificate.setStatus("Approved");
            certificate.setIssueDate(LocalDate.now());

            certificateRepository.save(certificate);
        }

        return "redirect:/certificates";
    }

    // Download approved certificate as PDF
    @GetMapping("/downloadCertificate")
    public ResponseEntity<byte[]> downloadCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository.findById(id).orElse(null);

        // Certificate must exist
        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }

        // Certificate must be approved
        if (!"Approved".equalsIgnoreCase(certificate.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document = new Document(
                    PageSize.A4,
                    50,
                    50,
                    50,
                    50
            );

            PdfWriter.getInstance(document, outputStream);

            document.open();

            // Fonts
            Font titleFont = new Font(
                    Font.HELVETICA,
                    22,
                    Font.BOLD
            );

            Font headingFont = new Font(
                    Font.HELVETICA,
                    16,
                    Font.BOLD
            );

            Font normalFont = new Font(
                    Font.HELVETICA,
                    12,
                    Font.NORMAL
            );

            // Header
            Paragraph header = new Paragraph(
                    "CIVICPULSE",
                    titleFont
            );

            header.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph(
                    "Smart Governance Platform",
                    normalFont
            );

            subHeader.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(subHeader);

            document.add(new Paragraph(" "));

            // Certificate title
            Paragraph certificateTitle = new Paragraph(
                    certificate.getCertificateType().toUpperCase(),
                    headingFont
            );

            certificateTitle.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(certificateTitle);

            document.add(new Paragraph(" "));

            // Certificate details
            document.add(new Paragraph(
                    "Certificate Number: "
                            + certificate.getApplicationId(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Applicant Name: "
                            + certificate.getApplicantName(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Certificate Type: "
                            + certificate.getCertificateType(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Department: "
                            + certificate.getDepartment(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Verification Status: "
                            + certificate.getVerificationStatus(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Application Status: "
                            + certificate.getStatus(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Issue Date: "
                            + certificate.getIssueDate(),
                    normalFont
            ));

            document.add(new Paragraph(" "));

            // Certificate statement
            Paragraph statement = new Paragraph(
                    "This certificate has been digitally generated "
                    + "through the CivicPulse Smart Governance Platform "
                    + "after successful verification and approval.",
                    normalFont
            );

            document.add(statement);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Authorized officer
            Paragraph officer = new Paragraph(
                    "Authorized Officer",
                    headingFont
            );

            officer.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(officer);

            Paragraph platform = new Paragraph(
                    "CivicPulse Smart Governance Platform",
                    normalFont
            );

            platform.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(platform);

            document.close();

            byte[] pdfBytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename="
                                    + certificate.getApplicationId()
                                    + ".pdf"
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().build();
        }
    }
}