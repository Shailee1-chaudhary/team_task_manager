package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(ApiResponse.success("API is running"));
    }
}
