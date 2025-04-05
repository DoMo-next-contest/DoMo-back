package next.domo.user.dto;

import lombok.Getter;

@Getter
public class ChangePasswordRequestDto {
    private String oldPassword;
    private String newPassword;
}
