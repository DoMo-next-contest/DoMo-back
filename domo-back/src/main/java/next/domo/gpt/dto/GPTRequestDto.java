package next.domo.gpt.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GPTRequestDto {
    private String projectName;
    private String projectDescription;
    private String projectRequirement;
    private LocalDateTime projectDeadline;
}

