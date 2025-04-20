package next.domo.user.dto;

import lombok.Getter;
import next.domo.user.enums.OnboardingTagType;
import next.domo.user.enums.TaskDetailPreference;
import next.domo.user.enums.WorkPace;

import java.util.List;

@Getter
public class UserOnboardingRequestDto {
    private TaskDetailPreference detailPreference;
    private WorkPace workPace;
    private List<OnboardingTagType> interestedTags;
}