package next.domo.file.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import next.domo.file.dto.ItemResponseDto;
import next.domo.file.entity.Item;
import next.domo.file.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponseDto::from)
                .toList();
    }

    public ItemResponseDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 item이 존재하지 않습니다."));

        return ItemResponseDto.from(item);
    }
}
