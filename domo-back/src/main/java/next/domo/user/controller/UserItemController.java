package next.domo.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import next.domo.file.dto.ItemResponseDto;
import next.domo.user.dto.UserItemStoreResponseDto;
import next.domo.user.service.UserItemService;
import next.domo.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-items")
public class UserItemController {
    private final UserService userService;
    private final UserItemService userItemService;

    @Operation(summary = "사용자 아이템 선택 (유저 아이템 등록)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 아이템 선택 성공"),
            @ApiResponse(responseCode = "4XX", description = "사용자 아이템 선택 실패")
    })
    @PostMapping("/{itemId}")
    public ResponseEntity<Long> selectItem(HttpServletRequest request, @Parameter(description = "추가할 item ID", required = true, example = "1") @PathVariable Long itemId) {
        Long userId = userService.getUserIdFromToken(request);
        userItemService.addItemToUser(userId, itemId);
        return ResponseEntity.ok(itemId);
    }

    @Operation(summary = "사용자의 item store 조회 (사용자 소유 여부 포함)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "조회시 요청 JSON 데이터 없음"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자의 item store 조회 성공"),
            @ApiResponse(responseCode = "4XX", description = "사용자의 item store 조회 실패")
    })
    @GetMapping("/store")
    public ResponseEntity<List<UserItemStoreResponseDto>> getStoreItems(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(userItemService.getStoreItemsByUser(userId));
    }

    @Operation(summary = "사용자가 최근에 사용한 item 조회",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "조회시 요청 JSON 데이터 없음"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자가 최근에 사용한 item 조회 성공"),
            @ApiResponse(responseCode = "4XX", description = "사용자가 최근에 사용한 item 조회 실패")
    })
    @GetMapping("/recent")
    public ResponseEntity<ItemResponseDto> getLatestEquippedItem(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        return ResponseEntity.ok(userItemService.getLatestEquippedItem(userId));
    }


}
