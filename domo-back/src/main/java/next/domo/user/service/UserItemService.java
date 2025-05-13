package next.domo.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import next.domo.file.entity.Item;
import next.domo.file.repository.ItemRepository;
import next.domo.user.dto.UserItemStoreResponseDto;
import next.domo.user.entity.User;
import next.domo.user.entity.UserItem;
import next.domo.user.repository.UserItemRepository;
import next.domo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;

    @Transactional
    public void addItemToUser(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        UserItem userItem = UserItem.builder()
                .user(user)
                .item(item)
                .build();

        userItemRepository.save(userItem);
    }

    public List<UserItemStoreResponseDto> getStoreItemsByUser(Long userId) {
        List<Item> allItems = itemRepository.findAll();
        List<Long> ownedItemIds = userItemRepository.findItemIdsByUserId(userId);

        return allItems.stream()
                .map(item -> UserItemStoreResponseDto.from(item, ownedItemIds.contains(item.getItemId())))
                .toList();
    }
}
