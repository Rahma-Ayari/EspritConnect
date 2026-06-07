package tn.esprit.espritconnect2.Service;

import java.time.LocalDateTime;

public interface IReportingService {
    byte[] exportTicketsCsv(LocalDateTime from, LocalDateTime to);

}
