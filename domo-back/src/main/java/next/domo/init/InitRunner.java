package next.domo.init;

import lombok.RequiredArgsConstructor;
import next.domo.file.ItemInitializerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitRunner implements CommandLineRunner {

    private final ItemInitializerService itemInitializerService;

    @Override
    public void run(String... args) {
        itemInitializerService.initializeItemsFromS3();
    }
}
