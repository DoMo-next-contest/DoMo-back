package next.domo.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import next.domo.file.dto.ItemResponseDto;
import next.domo.file.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;

    @Operation(summary = "아이템 전체 조회",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "조회시 요청 JSON 데이터 없음"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이템 전체 조회 성공"),
            @ApiResponse(responseCode = "4XX", description = "아이템 전체 조회 실패")
    })
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }
}
