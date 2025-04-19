package next.domo.project.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectLevelType {
    상(60),
    중(50),
    하(40);

    private final int factor;

    ProjectLevelType(int factor) {
        this.factor = factor;
    }

    public int getFactor() {
        return factor;
    }

    // 문자열 → enum 매핑 (JSON 요청에서도 인식되게끔)
    @JsonCreator
    public static ProjectLevelType from(String value) {
        return switch (value) {
            case "상" -> 상;
            case "중" -> 중;
            case "하" -> 하;
            default -> throw new IllegalArgumentException("유효하지 않은 난이도입니다: " + value);
        };
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
