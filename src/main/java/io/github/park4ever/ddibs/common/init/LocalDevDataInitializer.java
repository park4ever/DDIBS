package io.github.park4ever.ddibs.common.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ddibs.dev-data", name = "enabled", havingValue = "true")
public class LocalDevDataInitializer implements ApplicationRunner {

    private final LocalDevDataService localDevDataService;

    @Override
    public void run(ApplicationArguments args) {
        localDevDataService.initialize();
        log.info("로컬 개발용 초기 데이터 점검 및 생성이 완료되었습니다.");
    }
}
