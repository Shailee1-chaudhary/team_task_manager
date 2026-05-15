package com.teamtask.controller;

import com.teamtask.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.UUID;

@Controller
public class HomeController {

    // Unique ID generated on each server startup
    private static final String SERVER_INSTANCE_ID = UUID.randomUUID().toString();

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(ApiResponse.success("API is running",
                Map.of("serverInstanceId", SERVER_INSTANCE_ID)));
    }
}
