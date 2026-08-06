package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Complaint;
import com.example.demo.repository.ComplaintRepository;

@Controller
public class OfficerController {

    @Autowired
    private ComplaintRepository complaintRepository;

    // Officer Login Page
    @GetMapping("/officer")
    public String officerLoginPage() {
        return "officerLogin";
    }

    // Officer Login
    @PostMapping("/officerLogin")
    public String officerLogin(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {

        // Temporary hardcoded login
        if (username.equals("officer") && password.equals("officer123")) {

            List<Complaint> complaints = complaintRepository.findAll();
            model.addAttribute("complaints", complaints);

            return "officerDashboard";
        }

        model.addAttribute("error", "Invalid Username or Password");
        return "officerLogin";
    }

    // View All Assigned Complaints
    @GetMapping("/officerComplaints")
    public String officerComplaints(Model model) {

        List<Complaint> complaints = complaintRepository.findAll();
        model.addAttribute("complaints", complaints);

        return "officerDashboard";
    }

    // Open Update Status Page
    @GetMapping("/updateStatus/{id}")
    public String updateStatusPage(@PathVariable int id, Model model) {

        Complaint complaint = complaintRepository.findById(id).orElse(null);

        if (complaint == null) {
            return "redirect:/officerComplaints";
        }

        model.addAttribute("complaint", complaint);

        return "updateComplaintStatus";
    }

    // Save Updated Status
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam int id,
                               @RequestParam String status) {

        Complaint complaint = complaintRepository.findById(id).orElse(null);

        if (complaint != null) {
            complaint.setStatus(status);
            complaintRepository.save(complaint);
        }

        return "redirect:/officerComplaints";
    }
}