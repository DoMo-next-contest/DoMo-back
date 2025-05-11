package next.domo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserInfoDto {
    private String loginId;
    private String name;
    private String email;
    private int userCoin;
}
