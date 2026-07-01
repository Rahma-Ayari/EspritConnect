package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.util.List;

@Data
public class JobOfferDTO {
    private Long id;
    private String title;
    private String contractType;
    private String department;
    private String experienceLevel;
    private Integer numberOfPositions;
    private String workMode;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private String duration;
    private String deadline;
    private List<String> requiredSkills;
    private List<String> technologies;
    private List<String> languages;
    private String description;
    private String responsibilities;
    private String requirements;
    private String benefits;
    private String status;
    private Integer applicationCount;
    private Boolean isPinned;
    private Boolean isArchived;
    private Long entrepriseId;
    private String companyName;
    private String createdAt;
}
