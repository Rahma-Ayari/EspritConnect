package tn.esprit.espritconnect2.DTO;

import lombok.Data;

@Data
public class FAQRequestDTO {
    private String question;
    private String answer;
    private Long categoryId;
}
