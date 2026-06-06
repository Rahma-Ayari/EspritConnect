package tn.esprit.espritconnect2.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FAQDTO {
    private Long id;
    private String question;
    private String answer;

    private Long categoryId;
    private String categoryName;

    private String authorName;

    private boolean isImportant;
    private int viewCount;
    private int helpfulCount;
    private int notHelpfulCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
