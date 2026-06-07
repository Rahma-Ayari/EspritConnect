package tn.esprit.espritconnect2.Controller.emailBackOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.emailBackOffice.DigestPreviewResponseDTO;
import tn.esprit.espritconnect2.Service.emailBackOffice.ActivityDigestSendService;

@RestController
@RequestMapping("/api/activity-digest")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivityDigestOpsController {

    private final ActivityDigestSendService sendService;

    @GetMapping("/preview")
    public ResponseEntity<DigestPreviewResponseDTO> preview() {
        return ResponseEntity.ok(DigestPreviewResponseDTO.builder()
                .html(sendService.buildPreviewHtml())
                .build());
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNow() {
        sendService.sendNowToAllEnabledUsers();
        return ResponseEntity.ok().build();
    }
}