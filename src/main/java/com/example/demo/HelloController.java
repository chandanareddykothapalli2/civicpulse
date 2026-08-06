package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.repository.ComplaintRepository;

@Controller
public class HelloController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @GetMapping("/")
    public String home() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String name, Model model) {

        // Default name if URL doesn't contain ?name=
        if (name == null || name.trim().isEmpty()) {
            name = "Chandana";
        }

        model.addAttribute("name", name);

        long total = complaintRepository.count();
        long pending = complaintRepository.countByStatus("Pending");
        long resolved = complaintRepository.countByStatus("Resolved");
        long highPriority = complaintRepository.findAll()
                .stream()
                .filter(c -> "High".equalsIgnoreCase(c.getPriority()))
                .count();

        long departments = 5;

        model.addAttribute("total", total);
        model.addAttribute("pending", pending);
        model.addAttribute("resolved", resolved);
        model.addAttribute("highPriority", highPriority);
        model.addAttribute("departments", departments);

        return "dashboard";
    }
}