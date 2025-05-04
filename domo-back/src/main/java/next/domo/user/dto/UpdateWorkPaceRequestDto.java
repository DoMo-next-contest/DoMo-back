package next.domo.user.dto;

import lombok.Getter;
import next.domo.user.enums.WorkPace;

@Getter
public class UpdateWorkPaceRequestDto {
    private WorkPace workPace;

}
