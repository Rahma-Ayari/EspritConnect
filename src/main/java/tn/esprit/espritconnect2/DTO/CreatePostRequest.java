package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import tn.esprit.espritconnect2.Entitie.PostType;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreatePostRequest {
    private String title;
    private String content;
    private Long categoryId;
    private PostType postType;
    private List<String> tags = new ArrayList<>();
    private String website;
    private String coverImageUrl;
    private String videoUrl;
    private String pdfUrl;
    private String pdfExtractedText;
    private boolean allowMentions = true;
    private boolean aiGenerated;
    private boolean draft;
    private String authorName;
    private String authorEmail;
    private Role authorRole;
    private Long discussionId;
}
