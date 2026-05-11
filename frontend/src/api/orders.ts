import { apiRequest } from "./client";
import type {
    AdminOrderSearchRequest,
    AdminOrderSummaryResponse,
    PageResponse,
} from "../types/order";

function buildOrderSearchParams(condition: AdminOrderSearchRequest, page: number, size: number) {
    const params = new URLSearchParams();

    if (condition.orderCode) params.set("orderCode", condition.orderCode);
    if (condition.status) params.set("status", condition.status);
    if (condition.sellerId !== undefined) params.set("sellerId", String(condition.sellerId));
    if (condition.memberId !== undefined) params.set("memberId", String(condition.memberId));
    if (condition.memberEmailKeyword) params.set("memberEmailKeyword", condition.memberEmailKeyword);
    if (condition.productNameKeyword) params.set("productNameKeyword", condition.productNameKeyword);
    if (condition.from) params.set("from", condition.from);
    if (condition.to) params.set("to", condition.to);

    params.set("page", String(page));
    params.set("size", String(size));
    params.set("sort", "createdAt,desc");

    return params.toString();
}

export async function getAdminOrders(
    condition: AdminOrderSearchRequest,
    page = 0,
    size = 10,
): Promise<PageResponse<AdminOrderSummaryResponse>> {
    const query = buildOrderSearchParams(condition, page, size);

    return apiRequest<PageResponse<AdminOrderSummaryResponse>>(
        `/api/admin/orders?${query}`,
        {
            method: "GET",
        },
    );
}