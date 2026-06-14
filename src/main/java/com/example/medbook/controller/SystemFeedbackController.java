package com.example.medbook.controller;

import com.example.medbook.dto.request.SystemFeedbackRequest;
import com.example.medbook.dto.response.MessageResponse;
import com.example.medbook.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/system-feedback")
    public ResponseEntity<MessageResponse> submitSystemFeedback(
            @Valid @RequestBody SystemFeedbackRequest request) {
        return feedbackService.submitSystemFeedback(request);
    }
}
