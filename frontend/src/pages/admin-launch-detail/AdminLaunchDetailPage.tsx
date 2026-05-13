import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { ApiError } from "../../api/client";
import { getAdminLaunchDetail } from "../../api/launches";
import EmptyState from "../../components/EmptyState";
import LoadingBox from "../../components/LoadingBox";
import PageHeader from "../../components/PageHeader";
import type { AdminLaunchDetailResponse } from "../../types/launch";
import { formatDateTime, formatPrice } from "../../utils/format";

export default function AdminLaunchDetailPage() {
    const { launchId } = useParams();
    const [launchDetail, setLaunchDetail] = useState<AdminLaunchDetailResponse | null>(
        null,
    );
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        void fetchLaunchDetail();
    }, [launchId]);

    async function fetchLaunchDetail() {
        if (!launchId) {
            setErrorMessage("발매 ID가 올바르지 않습니다.");
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
        setErrorMessage("");

        try {
            const response = await getAdminLaunchDetail(Number(launchId));
            setLaunchDetail(response);
        } catch (error) {
            if (error instanceof ApiError) {
                setErrorMessage(error.message);
            } else {
                setErrorMessage("발매 상세를 불러오는 중 오류가 발생했습니다.");
            }
        } finally {
            setIsLoading(false);
        }
    }

    if (isLoading) {
        return <LoadingBox message="발매 상세를 불러오는 중입니다..." />;
    }

    if (errorMessage) {
        return (
            <EmptyState
                title="발매 상세를 불러오지 못했습니다."
                description={errorMessage}
            />
        );
    }

    if (!launchDetail) {
        return (
            <EmptyState
                title="발매 상세 정보가 없습니다."
                description="다시 시도해보세요."
            />
        );
    }

    return (
        <div className="page-section">
            <PageHeader
                title="발매 상세"
                description="발매 기본 정보와 옵션별 재고 현황을 확인합니다."
            />

            <div className="page-card">
                <div className="detail-actions">
                    <Link to="/admin/launches" className="detail-back-link">
                        ← 발매 목록으로
                    </Link>
                </div>

                <div className="detail-grid">
                    <div className="detail-item">
                        <span className="detail-label">발매 코드</span>
                        <strong>{launchDetail.launchCode}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">발매명</span>
                        <strong>{launchDetail.launchName}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">상태</span>
                        <strong>{launchDetail.status}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">판매자</span>
                        <strong>
                            {launchDetail.sellerName} (ID: {launchDetail.sellerId})
                        </strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">상품</span>
                        <strong>
                            {launchDetail.productName} (ID: {launchDetail.productId})
                        </strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">발매 시작</span>
                        <strong>{formatDateTime(launchDetail.startAt)}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">발매 종료</span>
                        <strong>{formatDateTime(launchDetail.endAt)}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">생성 시각</span>
                        <strong>{formatDateTime(launchDetail.createdAt)}</strong>
                    </div>

                    <div className="detail-item">
                        <span className="detail-label">수정 시각</span>
                        <strong>{formatDateTime(launchDetail.updatedAt)}</strong>
                    </div>
                </div>
            </div>

            <div className="page-card">
                <PageHeader
                    title="옵션별 재고 현황"
                    description="옵션별 판매가, 총 재고, 가용 재고를 확인합니다."
                />

                {launchDetail.variants.length === 0 ? (
                    <EmptyState
                        title="발매 옵션이 없습니다."
                        description="해당 발매에 연결된 발매 옵션이 없습니다."
                    />
                ) : (
                    <div className="table-wrapper">
                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>발매 옵션 ID</th>
                                <th>상품 옵션 ID</th>
                                <th>옵션명</th>
                                <th>판매가</th>
                                <th>총 재고</th>
                                <th>가용 재고</th>
                            </tr>
                            </thead>
                            <tbody>
                            {launchDetail.variants.map((variant) => (
                                <tr key={variant.launchVariantId}>
                                    <td>{variant.launchVariantId}</td>
                                    <td>{variant.productVariantId}</td>
                                    <td>{variant.variantName}</td>
                                    <td>{formatPrice(variant.salePrice)}</td>
                                    <td>{variant.totalStock}</td>
                                    <td>{variant.availableStock}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}