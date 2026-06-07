package tn.esprit.espritconnect2.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChatbotRequestDTO {

    private String message;

    /** Optional prior turns: role = "user" | "assistant" */
    private List<ChatbotHistoryItemDTO> history = new ArrayList<>();
}
