package next.domo.gpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import next.domo.project.entity.Project;
import next.domo.project.entity.ProjectLevelType;
import next.domo.project.repository.ProjectRepository;
import next.domo.subtask.entity.SubTask;
import next.domo.subtask.entity.SubTaskTag;
import next.domo.subtask.repository.SubTaskRepository;
import next.domo.user.entity.User;
import next.domo.user.entity.UserTag;
import next.domo.user.repository.UserRepository;
import next.domo.user.repository.UserTagRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static next.domo.subtask.entity.SubTaskTag.*;

@Slf4j
@Service
public class GPTService{

    private final WebClient webClient;
    private final UserTagRepository userTagRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SubTaskRepository subTaskRepository;
    private final String apiKey;

    public GPTService(@Value("${openai.api-key}") String apiKey, UserTagRepository userTagRepository, UserRepository userRepository, ProjectRepository projectRepository, SubTaskRepository subTaskRepository) {
        this.apiKey = apiKey;
        this.userTagRepository = userTagRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.subTaskRepository = subTaskRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // userProfile이 생기면 수정 (tagRate 관련)
    public String createSubTaskByGPT(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로젝트를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디를 가진 사용자를 찾을 수 없습니다."));
        List<UserTag> userTags = userTagRepository.findByUserUserId(userId);
        // userTag 퍼센트 저장 map
        Map<SubTaskTag, Float> tagRateMap = userTags.stream()
                .collect(Collectors.toMap(
                        UserTag::getSubTaskTag,
                        UserTag::getActualToExpectedRate
                ));

        // 각 태그별 수치 꺼내기 (없으면 100%)
        int documentation = (int) Math.floor(tagRateMap.getOrDefault(DOCUMENTATION, 1f)*100);
        int planning = (int) Math.floor(tagRateMap.getOrDefault(PLANNING_STRATEGY, 1f)*100);
        int development = (int) Math.floor(tagRateMap.getOrDefault(DEVELOPMENT, 1f)*100);
        int design = (int) Math.floor(tagRateMap.getOrDefault(DESIGN, 1f)*100);
        int research = (int) Math.floor(tagRateMap.getOrDefault(RESEARCH_ANALYSIS, 1f)*100);
        int communication = (int) Math.floor(tagRateMap.getOrDefault(COMMUNICATION, 1f)*100);
        int operations = (int) Math.floor(tagRateMap.getOrDefault(OPERATIONS, 1f)*100);
        int exercise = (int) Math.floor(tagRateMap.getOrDefault(EXERCISE, 1f)*100);
        int personal = (int) Math.floor(tagRateMap.getOrDefault(PERSONAL_LIFE, 1f)*100);

        // 프롬프트
        String userMessage = String.format("""
                        이 사용자는 세분화 선호도는 [%s]이고, 작업 여유 성향은 [%s]이야.
                        [%s]라는 프로젝트 이름과 [%s]이라는 프로젝트 설명, [%s]라는 프로젝트 요구사항, 그리고 현재 날짜는 [%s]이고 프로젝트 데드라인 [%s]을 갖는 프로젝트야.
                        
                        이 프로젝트의 실제 하위작업 리스트(subTaskList)를 만들어줘. 하위작업에는 다음 정보를 포함해줘:
                        하위작업 순서(subTaskOrder), 하위작업 제목(subTaskName), 하위작업 예상 소요시간(subTaskExpectedTime), 하위작업 태그(subTaskTag)
                        
                        각 하위작업에는 실제 사용자의 작업 성향을 고려해서 예상 소요시간을 조정해줘.
                        단, 예상 소요시간(subTaskExpectedTime)은 **분 단위(minute)**로 정수로 표현해줘.
                        사용자의 태그별 예측 대비 실제 소요 시간 비율은 다음과 같아:
                        - DOCUMENTATION: %d%%
                        - PLANNING_STRATEGY: %d%%
                        - DEVELOPMENT: %d%%
                        - DESIGN: %d%%
                        - RESEARCH_ANALYSIS: %d%%
                        - COMMUNICATION: %d%%
                        - OPERATIONS: %d%%
                        - EXERCISE: %d%%
                        - PERSONAL_LIFE: %d%%
                        
                        예를 들어 DEVELOPMENT 작업에서 130%%이면, 해당 작업은 평균보다 30%% 더 걸린다고 보면 돼.
                        이 소요율을 참고해서 각 하위작업의 예상 소요시간을 현실적으로 조정해줘.
                        
                        각 하위작업에 대해, 작업의 특성과 목적에 가장 잘 어울리는 태그를 다음 중에서 하나 골라서 지정해줘:
                        DOCUMENTATION, PLANNING_STRATEGY, DEVELOPMENT, DESIGN, RESEARCH_ANALYSIS, COMMUNICATION, OPERATIONS, EXERCISE, PERSONAL_LIFE
                       
                        단, subTaskName(작업 제목)은 반드시 한국어로 작성해줘. 나머지 데이터는 그대로 영어 형식을 유지해.
                        하위작업 리스트만 JSON 데이터 형식으로 응답해줘.
                """,
                user.getDetailPreference(),
                user.getWorkPace(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectRequirement(),
                LocalDate.now(),
                project.getProjectDeadline(),
                documentation,
                planning,
                development,
                design,
                research,
                communication,
                operations,
                exercise,
                personal
        );
        log.info(userMessage);
        Map<String, Object> message1 = Map.of(
                "role", "system",
                "content", "You are a helpful assistant."
        );

        Map<String, Object> message2 = Map.of(
                "role", "user",
                "content", userMessage
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(message1, message2));

        String rawResponse = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(rawResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // 하위 작업 리스트 추출
            JsonNode subTaskList = objectMapper.readTree(content).path("subTaskList");
            if (subTaskList.isMissingNode() || !subTaskList.isArray()) {
                throw new RuntimeException("GPT 응답에서 subTaskList가 누락되었습니다.");
            }

            return content;
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }

    public ProjectLevelType predictProjectLevelByGPT(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("해당 프로젝트를 찾을 수 없습니다."));

        LocalDate now = LocalDate.now();

        String userMessage = String.format("""
                너는 프로젝트 난이도를 예측해주는 AI야.

                아래 정보를 바탕으로 프로젝트의 난이도를 '상', '중', '하' 중 하나로 판단해줘.

                - 프로젝트 이름: %s
                - 프로젝트 설명: %s
                - 프로젝트 요구사항: %s
                - 오늘 날짜: %s
                - 마감 기한: %s
                - 예상 소요 시간 (분 단위): %d

                판단 기준은 다음과 같아:
                - 프로젝트 마감까지 시간이 촉박해서 마감기한 내에 프로젝트 해결이 어렵거나,
                - 예상 소요 시간이 너무 길거나,
                - 요구사항이 많고 복잡해 보이면 '상'
                - 일반적이고 평이한 수준이면 '중'
                - 간단하거나 여유롭고 쉬워 보이면 '하'

                결과는 반드시 다음 형식의 JSON으로 응답해:
                { "projectLevel": "상" }
                """,
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectRequirement(),
                now,
                project.getProjectDeadline(),
                project.getProjectExpectedTime()
        );

        Map<String, Object> message1 = Map.of(
                "role", "system",
                "content", "You are a helpful assistant."
        );
        Map<String, Object> message2 = Map.of(
                "role", "user",
                "content", userMessage
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(message1, message2));

        String rawResponse = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            String projectLevelStr = mapper.readTree(content).path("projectLevel").asText();

            ProjectLevelType level = ProjectLevelType.from(projectLevelStr);

            // 결과 저장
            project.setProjectLevel(level.getFactor());
            projectRepository.save(project);

            return level;
        } catch (Exception e) {
            throw new RuntimeException("GPT 난이도 예측 실패", e);
        }
    }
}
