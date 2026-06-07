package tn.esprit.espritconnect2.Controller.backoffice;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.espritconnect2.Config.ApiOfficePaths;
import tn.esprit.espritconnect2.Service.IReportingService;

import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiOfficePaths.BACK_REPORTS)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackReportingController {

    private final IReportingService reportingService;

    @GetMapping("/tickets/export")
    public ResponseEntity<byte[]> exportTicketsCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        byte[] data = reportingService.exportTicketsCsv(from, to);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.setContentDispositionFormData("attachment", "tickets_report.csv");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }


}
