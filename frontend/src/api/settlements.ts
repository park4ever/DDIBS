import { apiRequest } from "./client";
import type { PageResponse } from "../types/order";
import type {
    AdminSettlementSearchRequest,
    AdminSettlementSummaryResponse,
} from "../types/settlement";

function buildSettlementSearchParams(
    condition: AdminSettlementSearchRequest,
    page: number,
    size: number,
) {
    const params = new URLSearchParams();

    if (condition.settlementCode) {
        params.set("settlementCode", condition.settlementCode);
    }

    if (condition.orderCode) {
        params.set("orderCode", condition.orderCode);
    }

    if (condition.sellerId !== undefined) {
        params.set("sellerId", String(condition.sellerId));
    }

    if (condition.status) {
        params.set("status", condition.status);
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

export async function getAdminSettlements(
    condition: AdminSettlementSearchRequest,
    page = 0,
    size = 10,
): Promise<PageResponse<AdminSettlementSummaryResponse>> {
    const query = buildSettlementSearchParams(condition, page, size);

    return apiRequest<PageResponse<AdminSettlementSummaryResponse>>(
        `/api/admin/settlements?${query}`,
        {
            method: "GET",
        },
    );
}