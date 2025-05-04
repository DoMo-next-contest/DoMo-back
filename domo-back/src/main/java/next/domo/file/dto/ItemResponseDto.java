package next.domo.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import next.domo.file.entity.Item;

@Getter
@Builder
@AllArgsConstructor
public class ItemResponseDto {

    private Long itemId;
    private String itemName;
    private String itemImageUrl;

    public static ItemResponseDto from(Item item) {
        return ItemResponseDto.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .itemImageUrl(item.getItemImageUrl())
                .build();
    }
}
