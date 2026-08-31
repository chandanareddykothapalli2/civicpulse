package com.example.demo;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Controller
public class CertificateController {

    @Autowired
    private CertificateRepository certificateRepository;

    // =========================================================
    // OPEN CERTIFICATE APPLICATION PAGE
    // =========================================================

    @GetMapping("/certificate")
    public String certificatePage(Model model) {
        return "certificate";
    }

    // =========================================================
    // SUBMIT CERTIFICATE APPLICATION
    // =========================================================

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

        // Save first so that database generates ID
        Certificate savedCertificate =
                certificateRepository.save(certificate);

        // Generate application ID
        savedCertificate.setApplicationId(
                "CERT" + (1000 + savedCertificate.getId())
        );

        certificateRepository.save(savedCertificate);

        return "redirect:/certificates";
    }

    // =========================================================
    // VIEW ALL CERTIFICATE APPLICATIONS
    // =========================================================

    @GetMapping("/certificates")
    public String viewCertificates(Model model) {

        List<Certificate> certificates =
                certificateRepository.findAll();

        model.addAttribute("certificates", certificates);

        return "certificates";
    }

    // =========================================================
    // VERIFY CERTIFICATE
    // =========================================================

    @PostMapping("/verifyCertificate")
    public String verifyCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository
                        .findById(id)
                        .orElse(null);

        if (certificate != null) {

            certificate.setVerificationStatus("Verified");

            certificateRepository.save(certificate);
        }

        return "redirect:/certificates";
    }

    // =========================================================
    // APPROVE CERTIFICATE
    // =========================================================

    @PostMapping("/approveCertificate")
    public String approveCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository
                        .findById(id)
                        .orElse(null);

        /*
         * Certificate can be approved only after
         * successful verification.
         */
        if (certificate != null
                && "Verified".equalsIgnoreCase(
                        certificate.getVerificationStatus())) {

            certificate.setStatus("Approved");

            certificate.setIssueDate(
                    LocalDate.now()
            );

            certificateRepository.save(certificate);
        }

        return "redirect:/certificates";
    }

    // =========================================================
    // DOWNLOAD APPROVED CERTIFICATE AS PDF
    // =========================================================

    @GetMapping("/downloadCertificate")
    public ResponseEntity<byte[]> downloadCertificate(
            @RequestParam int id) {

        Certificate certificate =
                certificateRepository
                        .findById(id)
                        .orElse(null);

        // -----------------------------------------------------
        // CHECK 1: CERTIFICATE EXISTS
        // -----------------------------------------------------

        if (certificate == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        // -----------------------------------------------------
        // CHECK 2: CERTIFICATE MUST BE VERIFIED
        // -----------------------------------------------------

        if (!"Verified".equalsIgnoreCase(
                certificate.getVerificationStatus())) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        // -----------------------------------------------------
        // CHECK 3: CERTIFICATE MUST BE APPROVED
        // -----------------------------------------------------

        if (!"Approved".equalsIgnoreCase(
                certificate.getStatus())) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        try {

            // =================================================
            // CREATE PDF
            // =================================================

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(
                            PageSize.A4,
                            50,
                            50,
                            50,
                            50
                    );

            PdfWriter writer =
                    PdfWriter.getInstance(
                            document,
                            outputStream
                    );

            document.open();

            // =================================================
            // COLORS
            // =================================================

            Color civicBlue =
                    new Color(0, 102, 204);

            Color lightBlue =
                    new Color(232, 242, 252);

            Color green =
                    new Color(25, 135, 84);

            Color lightGreen =
                    new Color(225, 245, 234);

            Color darkGray =
                    new Color(70, 70, 70);

            // =================================================
            // PROFESSIONAL DOUBLE BORDER
            // =================================================

            PdfContentByte canvas =
                    writer.getDirectContent();

            // Outer border
            canvas.setColorStroke(civicBlue);
            canvas.setLineWidth(3);

            canvas.rectangle(
                    25,
                    25,
                    PageSize.A4.getWidth() - 50,
                    PageSize.A4.getHeight() - 50
            );

            canvas.stroke();

            // Inner border
            canvas.setColorStroke(
                    new Color(150, 190, 230)
            );

            canvas.setLineWidth(1);

            canvas.rectangle(
                    32,
                    32,
                    PageSize.A4.getWidth() - 64,
                    PageSize.A4.getHeight() - 64
            );

            canvas.stroke();

            // =================================================
            // FONTS
            // =================================================

            Font titleFont =
                    new Font(
                            Font.HELVETICA,
                            25,
                            Font.BOLD,
                            civicBlue
                    );

            Font subtitleFont =
                    new Font(
                            Font.HELVETICA,
                            11,
                            Font.NORMAL,
                            darkGray
                    );

            Font certificateTitleFont =
                    new Font(
                            Font.HELVETICA,
                            21,
                            Font.BOLD,
                            civicBlue
                    );

            Font labelFont =
                    new Font(
                            Font.HELVETICA,
                            11,
                            Font.BOLD,
                            civicBlue
                    );

            Font valueFont =
                    new Font(
                            Font.HELVETICA,
                            11,
                            Font.NORMAL,
                            Color.BLACK
                    );

            Font smallFont =
                    new Font(
                            Font.HELVETICA,
                            9,
                            Font.NORMAL,
                            darkGray
                    );

            Font statusFont =
                    new Font(
                            Font.HELVETICA,
                            11,
                            Font.BOLD,
                            green
                    );

            // =================================================
            // CIVICPULSE HEADER
            // =================================================

            Paragraph header =
                    new Paragraph(
                            "CIVICPULSE",
                            titleFont
                    );

            header.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(header);

            Paragraph subtitle =
                    new Paragraph(
                            "SMART GOVERNANCE PLATFORM",
                            subtitleFont
                    );

            subtitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(subtitle);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // CERTIFICATE TITLE
            // =================================================

            Paragraph certificateTitle =
                    new Paragraph(
                            certificate
                                    .getCertificateType()
                                    .toUpperCase(),
                            certificateTitleFont
                    );

            certificateTitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(certificateTitle);

            Paragraph digitalCertificate =
                    new Paragraph(
                            "DIGITAL GOVERNMENT CERTIFICATE",
                            smallFont
                    );

            digitalCertificate.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(digitalCertificate);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // CERTIFICATE NUMBER
            // =================================================

            PdfPTable numberTable =
                    new PdfPTable(1);

            numberTable.setWidthPercentage(100);

            PdfPCell numberCell =
                    new PdfPCell(
                            new Phrase(
                                    "CERTIFICATE NUMBER : "
                                            + certificate
                                                    .getApplicationId(),
                                    new Font(
                                            Font.HELVETICA,
                                            12,
                                            Font.BOLD,
                                            civicBlue
                                    )
                            )
                    );

            numberCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            numberCell.setBackgroundColor(
                    lightBlue
            );

            numberCell.setPadding(11);

            numberCell.setBorderColor(
                    civicBlue
            );

            numberTable.addCell(numberCell);

            document.add(numberTable);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // CERTIFICATE DETAILS TABLE
            // =================================================

            PdfPTable detailsTable =
                    new PdfPTable(2);

            detailsTable.setWidthPercentage(100);

            detailsTable.setWidths(
                    new float[]{35f, 65f}
            );

            addProfessionalDetailRow(
                    detailsTable,
                    "Applicant Name",
                    certificate.getApplicantName(),
                    labelFont,
                    valueFont,
                    lightBlue
            );

            addProfessionalDetailRow(
                    detailsTable,
                    "Certificate Type",
                    certificate.getCertificateType(),
                    labelFont,
                    valueFont,
                    lightBlue
            );

            addProfessionalDetailRow(
                    detailsTable,
                    "Department",
                    certificate.getDepartment(),
                    labelFont,
                    valueFont,
                    lightBlue
            );

            addProfessionalDetailRow(
                    detailsTable,
                    "Verification Status",
                    "VERIFIED",
                    labelFont,
                    statusFont,
                    lightGreen
            );

            addProfessionalDetailRow(
                    detailsTable,
                    "Application Status",
                    "APPROVED",
                    labelFont,
                    statusFont,
                    lightGreen
            );

            String issueDate = "";

            if (certificate.getIssueDate() != null) {

                issueDate =
                        certificate
                                .getIssueDate()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "dd-MM-yyyy"
                                        )
                                );
            }

            addProfessionalDetailRow(
                    detailsTable,
                    "Issue Date",
                    issueDate,
                    labelFont,
                    valueFont,
                    lightBlue
            );

            document.add(detailsTable);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // CERTIFICATE STATEMENT
            // =================================================

            Paragraph statement =
                    new Paragraph();

            statement.add(
                    new Chunk(
                            "This is to certify that ",
                            valueFont
                    )
            );

            statement.add(
                    new Chunk(
                            certificate.getApplicantName(),
                            new Font(
                                    Font.HELVETICA,
                                    11,
                                    Font.BOLD,
                                    civicBlue
                            )
                    )
            );

            statement.add(
                    new Chunk(
                            " has successfully completed "
                                    + "the verification and approval "
                                    + "process for the above certificate "
                                    + "through the CivicPulse Smart "
                                    + "Governance Platform.",
                            valueFont
                    )
            );

            statement.setLeading(18);

            document.add(statement);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // VERIFIED AND APPROVED SECTION
            // =================================================

            PdfPTable statusTable =
                    new PdfPTable(2);

            statusTable.setWidthPercentage(100);

            PdfPCell verifiedCell =
                    new PdfPCell(
                            new Phrase(
                                    "VERIFIED",
                                    statusFont
                            )
                    );

            verifiedCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            verifiedCell.setBackgroundColor(
                    lightGreen
            );

            verifiedCell.setPadding(10);

            PdfPCell approvedCell =
                    new PdfPCell(
                            new Phrase(
                                    "APPROVED",
                                    statusFont
                            )
                    );

            approvedCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            approvedCell.setBackgroundColor(
                    lightGreen
            );

            approvedCell.setPadding(10);

            statusTable.addCell(
                    verifiedCell
            );

            statusTable.addCell(
                    approvedCell
            );

            document.add(statusTable);

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // AUTHORIZED OFFICER SECTION
            // =================================================

            PdfPTable signatureTable =
                    new PdfPTable(2);

            signatureTable.setWidthPercentage(100);

            PdfPCell leftCell =
                    new PdfPCell(
                            new Phrase(
                                    "Digitally Generated\n"
                                            + "CivicPulse Platform",
                                    smallFont
                            )
                    );

            leftCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            leftCell.setHorizontalAlignment(
                    Element.ALIGN_LEFT
            );

            PdfPCell rightCell =
                    new PdfPCell(
                            new Phrase(
                                    "Authorized Officer\n"
                                            + certificate
                                                    .getDepartment(),
                                    new Font(
                                            Font.HELVETICA,
                                            10,
                                            Font.BOLD,
                                            darkGray
                                    )
                            )
                    );

            rightCell.setBorder(
                    PdfPCell.NO_BORDER
            );

            rightCell.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            signatureTable.addCell(
                    leftCell
            );

            signatureTable.addCell(
                    rightCell
            );

            document.add(signatureTable);

            document.add(
                    new Paragraph(" ")
            );

            // =================================================
            // FOOTER
            // =================================================

            PdfPTable footerTable =
                    new PdfPTable(1);

            footerTable.setWidthPercentage(100);

            PdfPCell footerCell =
                    new PdfPCell(
                            new Phrase(
                                    "This is a digitally generated "
                                            + "certificate issued through "
                                            + "the CivicPulse Smart "
                                            + "Governance Platform.\n"
                                            + "Certificate No: "
                                            + certificate
                                                    .getApplicationId(),
                                    smallFont
                            )
                    );

            footerCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            footerCell.setPadding(9);

            footerCell.setBackgroundColor(
                    lightBlue
            );

            footerCell.setBorderColor(
                    new Color(150, 190, 230)
            );

            footerTable.addCell(
                    footerCell
            );

            document.add(footerTable);

            // =================================================
            // CLOSE DOCUMENT
            // =================================================

            document.close();

            byte[] pdfBytes =
                    outputStream.toByteArray();

            return ResponseEntity
                    .ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename="
                                    + certificate
                                            .getApplicationId()
                                    + ".pdf"
                    )
                    .contentType(
                            MediaType.APPLICATION_PDF
                    )
                    .body(pdfBytes);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    // =========================================================
    // HELPER METHOD FOR DETAILS TABLE
    // =========================================================

    private void addProfessionalDetailRow(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont,
            Color backgroundColor) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont
                        )
                );

        labelCell.setPadding(9);

        labelCell.setBackgroundColor(
                backgroundColor
        );

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                value == null
                                        ? ""
                                        : value,
                                valueFont
                        )
                );

        valueCell.setPadding(9);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}