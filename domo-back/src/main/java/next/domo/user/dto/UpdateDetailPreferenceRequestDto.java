package next.domo.user.dto;

import lombok.Getter;
import next.domo.user.enums.TaskDetailPreference;

@Getter
public class UpdateDetailPreferenceRequestDto {
    private TaskDetailPreference detailPreference;
}
