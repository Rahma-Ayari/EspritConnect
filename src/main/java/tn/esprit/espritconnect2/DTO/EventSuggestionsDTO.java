package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EventSuggestionsDTO {
    private List<CategorySuggestion> suggestions;
}
