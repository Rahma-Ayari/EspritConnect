package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CategorySuggestion {
    private String category;
    private long interestedUsers;
    private java.util.List<EventMatch> matches;
}
