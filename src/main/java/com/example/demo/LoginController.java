package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    // Open Login Page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Login Validation
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password) {

        User user = userRepository.findByEmailAndPassword(email, password);

        if (user != null) {
            return "redirect:/dashboard?name=" + user.getName();
        }

        return "login";
    }
}