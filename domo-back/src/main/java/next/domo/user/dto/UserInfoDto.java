package next.domo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import next.domo.user.enums.TaskDetailPreference;
import next.domo.user.enums.WorkPace;

@AllArgsConstructor
@Getter
public class UserInfoDto {
    private String loginId;
    private String name;
    private String email;
    private int userCoin;
    private TaskDetailPreference detailPreference;
    private WorkPace workPace;
}