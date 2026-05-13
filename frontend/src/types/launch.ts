export type LaunchStatus =
    | "UPCOMING"
    | "OPEN"
    | "CLOSED"
    | "ENDED"
    | "CANCELLED";

export interface AdminLaunchSearchRequest {
    launchCode?: string;
    status?: LaunchStatus;
    sellerId?: number;
    productNameKeyword?: string;
    from?: string;
    to?: string;
}

export interface AdminLaunchSummaryResponse {
    id: number;
    launchCode: string;
    launchName: string;
    status: LaunchStatus;
    sellerId: number;
    sellerName: string;
    productId: number;
    productName: string;
    startAt: string;
    endAt: string;
    variantCount: number;
    totalStock: number;
    availableStock: number;
}

export interface AdminLaunchVariantStockResponse {
    launchVariantId: number;
    productVariantId: number;
    variantName: string;
    salePrice: number;
    totalStock: number;
    availableStock: number;
}

export interface AdminLaunchDetailResponse {
    id: number;
    launchCode: string;
    launchName: string;
    status: LaunchStatus;
    sellerId: number;
    sellerName: string;
    productId: number;
    productName: string;
    startAt: string;
    endAt: string;
    variants: AdminLaunchVariantStockResponse[];
    createdAt: string;
    updatedAt: string;
}