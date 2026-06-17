import { useEffect, useState, type FormEvent } from "react";
import { ApiError } from "../../api/client";
import { getAdminOrders } from "../../api/orders";
import EmptyState from "../../components/EmptyState";
import LoadingBox from "../../components/LoadingBox";
import PageHeader from "../../components/PageHeader";
import Pagination from "../../components/Pagination";
import StatusBadge from "../../components/StatusBadge";
import type {
    AdminOrderSearchRequest,
    AdminOrderSummaryResponse,
    OrderStatus,
} from "../../types/order";

const PAGE_SIZE = 10;

const initialFilterState = {
    orderCode: "",
    status: "",
    sellerId: "",
    memberId: "",
    memberEmailKeyword: "",
    productNameKeyword: "",
};

function normalizeText(value: string) {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : undefined;
}

export default function AdminOrdersPage() {
    const [filters, setFilters] = useState(initialFilterState);
    const [searchCondition, setSearchCondition] =
        useState<AdminOrderSearchRequest>({});
    const [page, setPage] = useState(0);
    const [orders, setOrders] = useState<AdminOrderSummaryResponse[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadOrders() {
            setIsLoading(true);
            setErrorMessage("");

            try {
                const response = await getAdminOrders(searchCondition, page, PAGE_SIZE);

                if (cancelled) {
                    return;
                }

                setOrders(response.content);
                setTotalPages(response.totalPages);
            } catch (error) {
                if (cancelled) {
                    return;
                }

                if (error instanceof ApiError) {
                    setErrorMessage(error.message);
                } else {
                    setErrorMessage("주문 목록을 불러오는 중 오류가 발생했습니다.");
                }
            } finally {
                if (!cancelled) {
                    setIsLoading(false);
                }
            }
        }

        void loadOrders();

        return () => {
            cancelled = true;
        };
    }, [searchCondition, page]);

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const nextCondition: AdminOrderSearchRequest = {
            orderCode: normalizeText(filters.orderCode),
            status: (filters.status as OrderStatus) || undefined,
            sellerId: normalizeText(filters.sellerId)
                ? Number(filters.sellerId)
                : undefined,
            memberId: normalizeText(filters.memberId)
                ? Number(filters.memberId)
                : undefined,
            memberEmailKeyword: normalizeText(filters.memberEmailKeyword),
            productNameKeyword: normalizeText(filters.productNameKeyword),
        };

        setPage(0);
        setSearchCondition(nextCondition);
    }

    function handleReset() {
        setFilters(initialFilterState);
        setPage(0);
        setSearchCondition({});
    }

    function handlePageChange(nextPage: number) {
        setPage(nextPage);
    }

    return (
        <div className="page-section">
            <PageHeader
                title="주문 조회"
                description="관리자 관점에서 주문 상태와 주문 흐름을 확인합니다."
            />

            <div className="page-card">
                <div className="filter-panel">
                    <div className="filter-panel__header">
                        <h3 className="filter-panel__title">검색 조건</h3>
                        <p className="filter-panel__description">
                            주문 코드, 상태, 회원, 판매자, 상품 기준으로 운영 데이터를 탐색합니다.
                        </p>
                    </div>

                    <form className="filter-form" onSubmit={handleSubmit}>
                        <label className="filter-field">
                            <span className="filter-field__label">주문 코드</span>
                            <input
                                type="text"
                                value={filters.orderCode}
                                onChange={(event) =>
                                    setFilters((prev) => ({ ...prev, orderCode: event.target.value }))
                                }
                            />
                        </label>

                        <label className="filter-field">
                            <span className="filter-field__label">주문 상태</span>
                            <select
                                value={filters.status}
                                onChange={(event) =>
                                    setFilters((prev) => ({ ...prev, status: event.target.value }))
                                }
                            >
                                <option value="">전체 상태</option>
                                <option value="CREATED">CREATED</option>
                                <option value="CONFIRMED">CONFIRMED</option>
                                <option value="PAYMENT_FAILED">PAYMENT_FAILED</option>
                                <option value="HOLD_EXPIRED">HOLD_EXPIRED</option>
                            </select>
                        </label>

                        <label className="filter-field">
                            <span className="filter-field__label">판매자 ID</span>
                            <input
                                type="text"
                                inputMode="numeric"
                                value={filters.sellerId}
                                onChange={(event) =>
                                    setFilters((prev) => ({ ...prev, sellerId: event.target.value }))
                                }
                            />
                        </label>

                        <label className="filter-field">
                            <span className="filter-field__label">회원 ID</span>
                            <input
                                type="text"
                                inputMode="numeric"
                                value={filters.memberId}
                                onChange={(event) =>
                                    setFilters((prev) => ({ ...prev, memberId: event.target.value }))
                                }
                            />
                        </label>

                        <label className="filter-field">
                            <span className="filter-field__label">회원 이메일</span>
                            <input
                                type="text"
                                value={filters.memberEmailKeyword}
                                onChange={(event) =>
                                    setFilters((prev) => ({
                                        ...prev,
                                        memberEmailKeyword: event.target.value,
                                    }))
                                }
                            />
                        </label>

                        <label className="filter-field">
                            <span className="filter-field__label">상품명</span>
                            <input
                                type="text"
                                value={filters.productNameKeyword}
                                onChange={(event) =>
                                    setFilters((prev) => ({
                                        ...prev,
                                        productNameKeyword: event.target.value,
                                    }))
                                }
                            />
                        </label>

                        <div className="filter-form__actions">
                            <button type="submit">조회</button>
                            <button type="button" className="secondary" onClick={handleReset}>
                                초기화
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            {isLoading ? <LoadingBox /> : null}

            {!isLoading && errorMessage ? (
                <EmptyState
                    title="주문 목록을 불러오지 못했습니다."
                    description={errorMessage}
                />
            ) : null}

            {!isLoading && !errorMessage && orders.length === 0 ? <EmptyState /> : null}

            {!isLoading && !errorMessage && orders.length > 0 ? (
                <div className="page-card">
                    <div className="table-wrapper">
                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>주문 코드</th>
                                <th>회원</th>
                                <th className="cell-center">판매자 ID</th>
                                <th>상품</th>
                                <th>옵션</th>
                                <th className="cell-center">수량</th>
                                <th className="cell-right">총 금액</th>
                                <th className="cell-center">상태</th>
                                <th className="cell-center">생성 시각</th>
                            </tr>
                            </thead>
                            <tbody>
                            {orders.map((order) => (
                                <tr key={order.id}>
                                    <td>{order.orderCode}</td>
                                    <td>
                                        <div>{order.memberName}</div>
                                        <div className="table-subtext">{order.memberEmail}</div>
                                    </td>
                                    <td className="cell-center">{order.sellerId}</td>
                                    <td>{order.productName}</td>
                                    <td>{order.variantName}</td>
                                    <td className="cell-center">{order.quantity}</td>
                                    <td className="cell-right">
                                        {Number(order.totalPrice).toLocaleString()}원
                                    </td>
                                    <td className="cell-center">
                                        <StatusBadge status={order.status} />
                                    </td>
                                    <td className="cell-center">
                                        {new Date(order.createdAt).toLocaleString()}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    <Pagination
                        page={page}
                        totalPages={totalPages}
                        onPageChange={handlePageChange}
                    />
                </div>
            ) : null}
        </div>
    );
}