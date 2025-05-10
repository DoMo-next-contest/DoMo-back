package next.domo.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectCompletionResponseDto {
    private String message;
    private int coin;
}