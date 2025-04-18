package next.domo.gpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import next.domo.gpt.dto.GPTRequestDto;
import next.domo.subtask.entity.SubTaskTag;
import next.domo.user.User;
import next.domo.user.UserRepository;
import next.domo.user.UserTag;
import next.domo.user.UserTagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GPTService{

    private final WebClient webClient;
    private final UserTagRepository userTagRepository;
    private final UserRepository userRepository;
    private final String apiKey;

    public GPTService(@Value("${openai.api-key}") String apiKey, UserTagRepository userTagRepository, UserRepository userRepository) {
        this.apiKey = apiKey;
        this.userTagRepository = userTagRepository;
        this.userRepository = userRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // userProfile이 생기면 수정 (tagRate 관련)
    public String createSubTaskByGPT(Long userId, GPTRequestDto gptRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디를 가진 사용자를 찾을 수 없습니다."));
        List<UserTag> userTags = userTagRepository.findByUserUserId(userId);
        Map<SubTaskTag, Integer> tagRateMap = userTags.stream()
                .collect(Collectors.toMap(
                        UserTag::getUserTagName,
                        UserTag::getActualToExpectedRate
                ));

        // 2. 각 태그별 수치 꺼내기 (없으면 100%)
        int documentation = tagRateMap.getOrDefault("DOCUMENTATION", 100);
        int planning = tagRateMap.getOrDefault("PLANNING_STRATEGY", 100);
        int development = tagRateMap.getOrDefault("DEVELOPMENT", 100);
        int design = tagRateMap.getOrDefault("DESIGN", 100);
        int research = tagRateMap.getOrDefault("RESEARCH_ANALYSIS", 100);
        int communication = tagRateMap.getOrDefault("COMMUNICATION", 100);
        int operations = tagRateMap.getOrDefault("OPERATIONS", 100);
        int exercise = tagRateMap.getOrDefault("EXERCISE", 100);
        int personal = tagRateMap.getOrDefault("PERSONAL_LIFE", 100);

        String userMessage = String.format("""
                        이 사용자는 세분화 선호도는 [%d]이고, 작업 여유 성향은 [%d]이야.
                        [%s]라는 프로젝트 이름과 [%s]이라는 프로젝트 설명, [%s]라는 프로젝트 요구사항, 그리고 프로젝트 데드라인 [%s]을 갖는 프로젝트야.
                        
                        이 프로젝트의 실제 하위작업(subTask) 리스트를 만들어줘. 하위작업에는 다음 정보를 포함해줘:
                        - 하위작업 순서(subTaskOrder)
                        - 하위작업 제목(subTaskName)
                        - 하위작업 예상 소요시간(subTaskExpectedTime)
                        - 하위작업 태그(subTaskTag)
                        
                        각 하위작업에는 실제 사용자의 작업 성향을 고려해서 예상 소요시간을 조정해줘.
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
                gptRequestDto.getProjectName(),
                gptRequestDto.getProjectDescription(),
                gptRequestDto.getProjectRequirement(),
                gptRequestDto.getProjectDeadline(),
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
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }


    }
}
