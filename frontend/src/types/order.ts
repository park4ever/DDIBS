export type OrderStatus =
    | "CREATED"
    | "CONFIRMED"
    | "PAYMENT_FAILED"
    | "HOLD_EXPIRED";

export interface AdminOrderSearchRequest {
    orderCode?: string;
    status?: OrderStatus;
    sellerId?: number;
    memberId?: number;
    memberEmailKeyword?: string;
    productNameKeyword?: string;
    from?: string;
    to?: string;
}

export interface AdminOrderSummaryResponse {
    id: number;
    orderCode: string;
    memberId: number;
    memberEmail: string;
    memberName: string;
    sellerId: number;
    productName: string;
    variantName: string;
    quantity: number;
    totalPrice: number;
    status: OrderStatus;
    createdAt: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}