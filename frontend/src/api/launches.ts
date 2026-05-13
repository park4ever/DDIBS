import { apiRequest } from "./client";
import type { PageResponse } from "../types/order";
import type {
    AdminLaunchDetailResponse,
    AdminLaunchSearchRequest,
    AdminLaunchSummaryResponse,
} from "../types/launch";

function buildLaunchSearchParams(
    condition: AdminLaunchSearchRequest,
    page: number,
    size: number,
) {
    const params = new URLSearchParams();

    if (condition.launchCode) {
        params.set("launchCode", condition.launchCode);
    }

    if (condition.status) {
        params.set("status", condition.status);
    }

    if (condition.sellerId !== undefined) {
        params.set("sellerId", String(condition.sellerId));
    }

    if (condition.productNameKeyword) {
        params.set("productNameKeyword", condition.productNameKeyword);
    }

    if (condition.from) {
        params.set("from", condition.from);
    }

    if (condition.to) {
        params.set("to", condition.to);
    }

    params.set("page", String(page));
    params.set("size", String(size));
    params.set("sort", "createdAt,desc");

    return params.toString();
}

export async function getAdminLaunches(
    condition: AdminLaunchSearchRequest,
    page = 0,
    size = 10,
): Promise<PageResponse<AdminLaunchSummaryResponse>> {
    const query = buildLaunchSearchParams(condition, page, size);

    return apiRequest<PageResponse<AdminLaunchSummaryResponse>>(
        `/api/admin/launches?${query}`,
        {
            method: "GET",
        },
    );
}

export async function getAdminLaunchDetail(
    launchId: number,
): Promise<AdminLaunchDetailResponse> {
    return apiRequest<AdminLaunchDetailResponse>(`/api/admin/launches/${launchId}`, {
        method: "GET",
    });
}