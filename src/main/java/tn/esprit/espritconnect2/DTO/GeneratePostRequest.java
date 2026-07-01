package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import tn.esprit.espritconnect2.Entitie.PostType;

import java.util.ArrayList;
import java.util.List;

@Data
public class GeneratePostRequest {
    private String title;
    private PostType postType;
    private List<String> tags = new ArrayList<>();
    private String outputLanguage = "en";
}
