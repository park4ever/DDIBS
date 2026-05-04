package io.github.park4ever.ddibs.launch.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchDetailResponse;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSearchRequest;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSummaryResponse;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLaunchQueryService {

    private final LaunchRepository launchRepository;

    public Page<AdminLaunchSummaryResponse> searchLaunches(
            AdminLaunchSearchRequest condition,
            Pageable pageable
    ) {
        return launchRepository.searchAdminLaunches(condition, pageable);
    }

    public AdminLaunchDetailResponse getLaunchDetail(Long launchId) {
        return launchRepository.findAdminLaunchDetail(launchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_NOT_FOUND));
    }
}
