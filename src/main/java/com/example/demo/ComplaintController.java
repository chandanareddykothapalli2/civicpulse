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

import com.example.demo.entity.Complaint;
import com.example.demo.repository.ComplaintRepository;

@Controller
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @GetMapping("/complaint")
    public String complaintPage() {
        return "complaint";
    }

    @PostMapping("/complaint")
    public String saveComplaint(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam String location) {

        Complaint complaint = new Complaint();

        complaint.setTitle(title);
        complaint.setCategory(category);
        complaint.setDescription(description);
        complaint.setLocation(location);
        complaint.setStatus("Pending");

        // Department, Officer, Priority & SLA Due Date
        if (category.equalsIgnoreCase("Road Issue")) {

            complaint.setDepartment("Road Department");
            complaint.setOfficer("Rajesh Kumar");
            complaint.setPriority("High");
            complaint.setDueDate(LocalDate.now().plusDays(1));

        } else if (category.equalsIgnoreCase("Garbage")) {

            complaint.setDepartment("Sanitation Department");
            complaint.setOfficer("Ramesh");
            complaint.setPriority("Medium");
            complaint.setDueDate(LocalDate.now().plusDays(3));

        } else if (category.equalsIgnoreCase("Water Supply")) {

            complaint.setDepartment("Water Department");
            complaint.setOfficer("Suresh Reddy");
            complaint.setPriority("High");
            complaint.setDueDate(LocalDate.now().plusDays(1));

        } else if (category.equalsIgnoreCase("Street Light")) {

            complaint.setDepartment("Electricity Department");
            complaint.setOfficer("Mahesh Kumar");
            complaint.setPriority("Medium");
            complaint.setDueDate(LocalDate.now().plusDays(3));

        } else if (category.equalsIgnoreCase("Drainage")) {

            complaint.setDepartment("Drainage Department");
            complaint.setOfficer("Prakash");
            complaint.setPriority("High");
            complaint.setDueDate(LocalDate.now().plusDays(1));

        } else {

            complaint.setDepartment("General Department");
            complaint.setOfficer("Not Assigned");
            complaint.setPriority("Low");
            complaint.setDueDate(LocalDate.now().plusDays(7));
        }

        // Save complaint
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Generate Tracking ID
        savedComplaint.setTrackingId("CP" + (1000 + savedComplaint.getId()));

        complaintRepository.save(savedComplaint);

        return "redirect:/dashboard";
    }

    @GetMapping("/viewComplaints")
    public String viewComplaints(Model model) {

        List<Complaint> complaints = complaintRepository.findAll();

        for (Complaint complaint : complaints) {

            if (complaint.getDueDate() != null
                    && complaint.getDueDate().isBefore(LocalDate.now())
                    && !complaint.getStatus().equalsIgnoreCase("Resolved")
                    && !complaint.getStatus().equalsIgnoreCase("Escalated")) {

                complaint.setStatus("Escalated");
                complaintRepository.save(complaint);
            }
        }

        model.addAttribute("complaints", complaints);

        return "viewComplaints";
    }

    @GetMapping("/editComplaint/{id}")
    public String editComplaint(@PathVariable int id, Model model) {

        Complaint complaint = complaintRepository.findById(id).orElse(null);

        model.addAttribute("complaint", complaint);

        return "editComplaint";
    }

    @PostMapping("/updateComplaint")
    public String updateComplaint(
            @RequestParam int id,
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String status) {

        Complaint complaint = complaintRepository.findById(id).orElse(null);

        if (complaint != null) {

            complaint.setTitle(title);
            complaint.setCategory(category);
            complaint.setDescription(description);
            complaint.setLocation(location);
            complaint.setStatus(status);

            if (category.equalsIgnoreCase("Road Issue")) {

                complaint.setDepartment("Road Department");
                complaint.setOfficer("Rajesh Kumar");
                complaint.setPriority("High");
                complaint.setDueDate(LocalDate.now().plusDays(1));

            } else if (category.equalsIgnoreCase("Garbage")) {

                complaint.setDepartment("Sanitation Department");
                complaint.setOfficer("Ramesh");
                complaint.setPriority("Medium");
                complaint.setDueDate(LocalDate.now().plusDays(3));

            } else if (category.equalsIgnoreCase("Water Supply")) {

                complaint.setDepartment("Water Department");
                complaint.setOfficer("Suresh Reddy");
                complaint.setPriority("High");
                complaint.setDueDate(LocalDate.now().plusDays(1));

            } else if (category.equalsIgnoreCase("Street Light")) {

                complaint.setDepartment("Electricity Department");
                complaint.setOfficer("Mahesh Kumar");
                complaint.setPriority("Medium");
                complaint.setDueDate(LocalDate.now().plusDays(3));

            } else if (category.equalsIgnoreCase("Drainage")) {

                complaint.setDepartment("Drainage Department");
                complaint.setOfficer("Prakash");
                complaint.setPriority("High");
                complaint.setDueDate(LocalDate.now().plusDays(1));

            } else {

                complaint.setDepartment("General Department");
                complaint.setOfficer("Not Assigned");
                complaint.setPriority("Low");
                complaint.setDueDate(LocalDate.now().plusDays(7));
            }

            complaintRepository.save(complaint);
        }

        return "redirect:/viewComplaints";
    }
    @GetMapping("/complaintDetails/{id}")
    public String complaintDetails(@PathVariable int id, Model model) {

        Complaint complaint = complaintRepository.findById(id).orElse(null);

        if (complaint == null) {
            return "redirect:/viewComplaints";
        }

        model.addAttribute("complaint", complaint);

        return "complaintDetails";
    }
    @GetMapping("/deleteComplaint/{id}")
    public String deleteComplaint(@PathVariable int id) {

        complaintRepository.deleteById(id);

        return "redirect:/viewComplaints";
    }
}