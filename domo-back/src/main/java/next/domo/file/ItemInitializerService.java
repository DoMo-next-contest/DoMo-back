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

        for (S3Object object : response.contents()) {
            String key = object.key();
            if (key.endsWith("/")) continue;
            String fileName = key.substring(key.lastIndexOf("/") + 1);
            String name = fileName.replace(".glb", "");

            if (itemRepository.existsByItemImageUrl("https://" + bucket + ".s3." + region + ".amazonaws.com/" + key)) continue;

            Item item = Item.builder()
                    .itemName(name)
                    .itemImageUrl("https://" + bucket + ".s3." + region + ".amazonaws.com/" + key)
                    .build();

            itemRepository.save(item);
        }
    }
}
