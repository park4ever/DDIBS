export type SettlementStatus = "CREATED" | "CONFIRMED";

export interface AdminSettlementSearchRequest {
    settlementCode?: string;
    orderCode?: string;
    sellerId?: number;
    status?: SettlementStatus;
    from?: string;
    to?: string;
}

export interface AdminSettlementSummaryResponse {
    id: number;
    orderId: number;
    orderCode: string;
    sellerId: number;
    settlementCode: string;
    settlementAmount: number;
    status: SettlementStatus;
    settledAt: string | null;
    createdAt: string;
}

export interface AdminSettlementDetailResponse {
    id: number;
    orderId: number;
    sellerId: number;
    settlementCode: string;
    settlementAmount: number;
    status: SettlementStatus;
    settledAt: string | null;
    createdAt: string;
    updatedAt: string;
}