package next.domo.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import next.domo.ai.dto.AIRequestDto;
import next.domo.ai.service.GPTService;
import next.domo.ai.service.LlamaService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIController {
    private final GPTService gptService;
    private final LlamaService llamaService;

    @PostMapping("/gpt/ask")
    public String askGPT(@RequestBody AIRequestDto request) {
        return gptService.ask(request.getMessage());
    }

    @PostMapping("/llama/ask")
    public String askLlama(@RequestBody AIRequestDto request) {
        return llamaService.ask(request.getMessage());
    }

}
