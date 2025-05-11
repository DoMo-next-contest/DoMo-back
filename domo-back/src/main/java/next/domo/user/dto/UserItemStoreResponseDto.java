package next.domo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import next.domo.file.entity.Item;

@Builder
@AllArgsConstructor
@Getter
public class UserItemStoreResponseDto {
    private Long id;
    private String name;
    private String imageUrl;
    private String image2dUrl;
    private boolean hasItem;

    public static UserItemStoreResponseDto from(Item item, boolean hasItem) {
        return UserItemStoreResponseDto.builder()
                .id(item.getItemId())
                .name(item.getItemName())
                .imageUrl(item.getItemImageUrl())
                .image2dUrl(item.getItem2dImageUrl())
                .hasItem(hasItem)
                .build();
    }
}
