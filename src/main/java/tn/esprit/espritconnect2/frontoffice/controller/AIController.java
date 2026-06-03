package tn.esprit.espritconnect2.frontoffice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.frontoffice.dto.AIDescriptionRequest;
import tn.esprit.espritconnect2.frontoffice.dto.AIDescriptionResponse;
import tn.esprit.espritconnect2.frontoffice.service.AIService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIController {
    
    private final AIService aiService;
    
    @PostMapping("/generate-description")
    public ResponseEntity<AIDescriptionResponse> generateDescription(@RequestBody AIDescriptionRequest request) {
        log.info("Received request to generate description for: {}", request.getTitre());
        
        AIDescriptionResponse response = aiService.generateJobDescription(request);
        
        return ResponseEntity.ok(response);
    }
}
