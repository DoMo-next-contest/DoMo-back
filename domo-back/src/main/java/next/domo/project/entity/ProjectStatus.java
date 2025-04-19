package next.domo.project.entity;

/*
 * - IN_PROGRESS: 진행 중
 * - ALMOST_DONE: 모든 하위작업이 완료되었지만, 난이도(level) 입력 전
 * - DONE: 하위작업 완료 + 난이도 입력 완료 → 코인 계산까지 완료된 상태
 */
public enum ProjectStatus {
    IN_PROGRESS,
    ALMOST_DONE,
    DONE
}
