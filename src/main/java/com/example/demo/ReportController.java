package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.repository.ComplaintRepository;

@Controller
public class ReportController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @GetMapping("/reports")
    public String reports(Model model) {

        model.addAttribute("totalComplaints",
                complaintRepository.count());

        model.addAttribute("pendingComplaints",
                complaintRepository.countByStatus("Pending"));

        model.addAttribute("resolvedComplaints",
                complaintRepository.countByStatus("Resolved"));

        model.addAttribute("inProgressComplaints",
                complaintRepository.countByStatus("In Progress"));

        model.addAttribute("escalatedComplaints",
                complaintRepository.countByStatus("Escalated"));

        model.addAttribute("roadComplaints",
                complaintRepository.countByDepartment("Road Department"));

        model.addAttribute("waterComplaints",
                complaintRepository.countByDepartment("Water Department"));

        model.addAttribute("electricityComplaints",
                complaintRepository.countByDepartment("Electricity Department"));

        model.addAttribute("sanitationComplaints",
                complaintRepository.countByDepartment("Sanitation Department"));

        model.addAttribute("drainageComplaints",
                complaintRepository.countByDepartment("Drainage Department"));

        model.addAttribute("highPriority",
                complaintRepository.countByPriority("High"));

        model.addAttribute("mediumPriority",
                complaintRepository.countByPriority("Medium"));

        model.addAttribute("lowPriority",
                complaintRepository.countByPriority("Low"));

        return "reports";
    }
}