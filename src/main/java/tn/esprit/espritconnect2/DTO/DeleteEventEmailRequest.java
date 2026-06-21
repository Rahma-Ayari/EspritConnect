package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteEventEmailRequest {
    private String subject;
    private String content;
}
