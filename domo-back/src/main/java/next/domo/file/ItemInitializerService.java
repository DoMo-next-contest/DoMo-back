package next.domo.file;

import lombok.RequiredArgsConstructor;
import next.domo.file.entity.Item;
import next.domo.file.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemInitializerService {

    private final ItemRepository itemRepository;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private final String folder = "items";

    public void initializeItemsFromS3() {
        S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        ListObjectsV2Response response = s3.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(folder + "/")
                .build());

        Map<String, Item> itemMap = new HashMap<>();

        for (S3Object object : response.contents()) {
            String key = object.key();
            if (key.endsWith("/")) continue;

            String fileName = key.substring(key.lastIndexOf("/") + 1);
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1); 

            String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

            itemMap.putIfAbsent(baseName, Item.builder().itemName(baseName).build());

            Item item = itemMap.get(baseName);

            if (extension.equals("glb")) {
                item = Item.builder()
                        .itemName(baseName)
                        .itemImageUrl(url)
                        .item2dImageUrl(item.getItem2dImageUrl())
                        .build();
            } else if (extension.equals("png")) {
                item = Item.builder()
                        .itemName(baseName)
                        .item2dImageUrl(url)
                        .itemImageUrl(item.getItemImageUrl())
                        .build();
            }

            itemMap.put(baseName, item);
        }

        for (Item item : itemMap.values()) {
            if (itemRepository.existsByItemName(item.getItemName())) continue;
            itemRepository.save(item);
        }
    }
}
