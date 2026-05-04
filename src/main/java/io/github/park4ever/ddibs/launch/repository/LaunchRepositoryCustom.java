package io.github.park4ever.ddibs.launch.repository;

import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchDetailResponse;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSearchRequest;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LaunchRepositoryCustom {

    Page<AdminLaunchSummaryResponse> searchAdminLaunches(
            AdminLaunchSearchRequest condition,
            Pageable pageable
    );

    Optional<AdminLaunchDetailResponse> findAdminLaunchDetail(Long launchId);
}
