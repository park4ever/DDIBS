package io.github.park4ever.ddibs.launch.repository;

import io.github.park4ever.ddibs.launch.domain.Launch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaunchRepository extends JpaRepository<Launch, Long> {

    Optional<Launch> findByLaunchCode(String launchCode);

    boolean existsByLaunchCode(String launchCode);
}
