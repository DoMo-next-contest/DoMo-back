package next.domo.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import next.domo.user.enums.TaskDetailPreference;
import next.domo.user.enums.WorkPace;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String loginId;
    private String password;
    private String name;
    private String email;
    private String refreshToken;
    private int userCoin;

    @Enumerated(EnumType.STRING)
    private TaskDetailPreference detailPreference;

    @Enumerated(EnumType.STRING)
    private WorkPace workPace;

    private String characterName;
    private String characterFileUrl;

    public void updateRefreshToken(String newRefreshToken){
        this.refreshToken = newRefreshToken;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void setDetailPreference(TaskDetailPreference detailPreference) {
        this.detailPreference = detailPreference;
    }
    
    public void setWorkPace(WorkPace workPace) {
        this.workPace = workPace;
    }

    public void addCoin(int coin) {
        this.userCoin += coin;
    }
    
    public void useCoin(int amount) {
        if (this.userCoin < amount) {
            int shortage = amount - this.userCoin;
            throw new IllegalArgumentException("보유 코인이 부족하여 뽑기를 진행할 수 없습니다. 현재 보유 코인: " 
                + this.userCoin + ", 부족한 코인: " + shortage);
        }
        this.userCoin -= amount;
    }

    public void setCharacterFileUrl(String url) {
        this.characterFileUrl = url;
    }
}
