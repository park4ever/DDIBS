import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { ApiError } from "../../api/client";
import { getAdminSettlementDetail } from "../../api/settlements";
import EmptyState from "../../components/EmptyState";
import LoadingBox from "../../components/LoadingBox";
import PageHeader from "../../components/PageHeader";
import StatusBadge from "../../components/StatusBadge";
import type { AdminSettlementDetailResponse } from "../../types/settlement";
import { formatDateTime, formatPrice } from "../../utils/format";

export default function AdminSettlementDetailPage() {
    const { settlementId } = useParams();
    const [settlementDetail, setSettlementDetail] =
        useState<AdminSettlementDetailResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        void fetchSettlementDetail();
    }, [settlementId]);

    async function fetchSettlementDetail() {
        if (!settlementId) {
            setErrorMessage("정산 ID가 올바르지 않습니다.");
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        setErrorMessage("");

        try {
            const response = await getAdminSettlementDetail(Number(settlementId));
            setSettlementDetail(response);
        } catch (error) {
            if (error instanceof ApiError) {
                setErrorMessage(error.message);
            } else {
                setErrorMessage("정산 상세를 불러오는 중 오류가 발생했습니다.");
            }
        } finally {
            setIsLoading(false);
        }
    }

    if (isLoading) {
        return <LoadingBox message="정산 상세를 불러오는 중입니다..." />;
    }

    if (errorMessage) {
        return (
            <EmptyState
                title="정산 상세를 불러오지 못했습니다."
                description={errorMessage}
            />
        );
    }

    if (!settlementDetail) {
        return (
            <EmptyState
                title="정산 상세 정보가 없습니다."
                description="다시 시도해보세요."
            />
        );
    }

    return (
        <div className="page-section">
            <PageHeader
                title="정산 상세"
                description="정산 기본 정보와 상태, 금액, 처리 시각을 확인합니다."
            />

            <div className="page-card">
                <div className="detail-actions">
                    <Link to="/admin/settlements" className="detail-back-link">
                        ← 정산 목록으로
                    </Link>
                </div>

                <div className="summary-cards">
                    <div className="summary-card">
                        <span className="summary-card__label">정산 금액</span>
                        <strong className="summary-card__value">
                            {formatPrice(settlementDetail.settlementAmount)}
                        </strong>
                    </div>

                    <div className="summary-card">
                        <span className="summary-card__label">상태</span>
                        <div className="summary-card__badge">
                            <StatusBadge status={settlementDetail.status} />
                        </div>
                    </div>

                    <div className="summary-card">
                        <span className="summary-card__label">정산 시각</span>
                        <strong className="summary-card__value">
                            {formatDateTime(settlementDetail.settledAt)}
                        </strong>
                    </div>
                </div>
            </div>

            <div className="page-card">
                <h3 className="detail-section-title">기본 정보</h3>

                <div className="detail-grid">
                    <div className="detail-item">
                        <span className="detail-label">정산 코드</span>
                        <strong>{settlementDetail.settlementCode}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">정산 ID</span>
                        <strong>{settlementDetail.id}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">주문 ID</span>
                        <strong>{settlementDetail.orderId}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">판매자 ID</span>
                        <strong>{settlementDetail.sellerId}</strong>
                    </div>
                </div>
            </div>

            <div className="page-card">
                <h3 className="detail-section-title">처리 정보</h3>

                <div className="detail-grid">
                    <div className="detail-item">
                        <span className="detail-label">상태</span>
                        <div>
                            <StatusBadge status={settlementDetail.status} />
                        </div>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">정산 금액</span>
                        <strong>{formatPrice(settlementDetail.settlementAmount)}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">정산 시각</span>
                        <strong>{formatDateTime(settlementDetail.settledAt)}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">생성 시각</span>
                        <strong>{formatDateTime(settlementDetail.createdAt)}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">수정 시각</span>
                        <strong>{formatDateTime(settlementDetail.updatedAt)}</strong>
                    </div>
                </div>
            </div>
        </div>
    );
}