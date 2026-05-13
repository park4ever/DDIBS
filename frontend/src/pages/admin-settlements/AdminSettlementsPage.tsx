import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router";
import { ApiError } from "../../api/client";
import { getAdminSettlements } from "../../api/settlements";
import EmptyState from "../../components/EmptyState";
import LoadingBox from "../../components/LoadingBox";
import PageHeader from "../../components/PageHeader";
import Pagination from "../../components/Pagination";
import StatusBadge from "../../components/StatusBadge";
import type {
    AdminSettlementSearchRequest,
    AdminSettlementSummaryResponse,
    SettlementStatus,
} from "../../types/settlement";
import { formatDateTime, formatPrice } from "../../utils/format";

const PAGE_SIZE = 10;

const initialFilterState = {
    settlementCode: "",
    orderCode: "",
    sellerId: "",
    status: "",
    from: "",
    to: "",
};

function normalizeText(value: string) {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : undefined;
}

export default function AdminSettlementsPage() {
    const [filters, setFilters] = useState(initialFilterState);
    const [searchCondition, setSearchCondition] =
        useState<AdminSettlementSearchRequest>({});
    const [page, setPage] = useState(0);
    const [settlements, setSettlements] = useState<AdminSettlementSummaryResponse[]>(
        [],
    );
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        void fetchSettlements();
    }, [searchCondition, page]);

    async function fetchSettlements() {
        setIsLoading(true);
        setErrorMessage("");

        try {
            const response = await getAdminSettlements(
                searchCondition,
                page,
                PAGE_SIZE,
            );
            setSettlements(response.content);
            setTotalPages(response.totalPages);
        } catch (error) {
            if (error instanceof ApiError) {
                setErrorMessage(error.message);
            } else {
                setErrorMessage("정산 목록을 불러오는 중 오류가 발생했습니다.");
            }
        } finally {
            setIsLoading(false);
        }
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const nextCondition: AdminSettlementSearchRequest = {
            settlementCode: normalizeText(filters.settlementCode),
            orderCode: normalizeText(filters.orderCode),
            sellerId: normalizeText(filters.sellerId)
                ? Number(filters.sellerId)
                : undefined,
            status: (filters.status as SettlementStatus) || undefined,
            from: normalizeText(filters.from),
            to: normalizeText(filters.to),
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
                title="정산 조회"
                description="확정 주문 기준으로 생성된 정산 내역을 조회합니다."
            />

            <div className="page-card">
                <form className="filter-form" onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="정산 코드"
                        value={filters.settlementCode}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                settlementCode: event.target.value,
                            }))
                        }
                    />

                    <input
                        type="text"
                        placeholder="주문 코드"
                        value={filters.orderCode}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                orderCode: event.target.value,
                            }))
                        }
                    />

                    <input
                        type="text"
                        inputMode="numeric"
                        placeholder="판매자 ID"
                        value={filters.sellerId}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                sellerId: event.target.value,
                            }))
                        }
                    />

                    <select
                        value={filters.status}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                status: event.target.value,
                            }))
                        }
                    >
                        <option value="">전체 상태</option>
                        <option value="CREATED">CREATED</option>
                        <option value="CONFIRMED">CONFIRMED</option>
                    </select>

                    <input
                        type="datetime-local"
                        value={filters.from}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                from: event.target.value,
                            }))
                        }
                    />

                    <input
                        type="datetime-local"
                        value={filters.to}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                to: event.target.value,
                            }))
                        }
                    />

                    <div className="filter-form__actions">
                        <button type="submit">조회</button>
                        <button type="button" className="secondary" onClick={handleReset}>
                            초기화
                        </button>
                    </div>
                </form>
            </div>

            {isLoading ? <LoadingBox /> : null}

            {!isLoading && errorMessage ? (
                <EmptyState
                    title="정산 목록을 불러오지 못했습니다."
                    description={errorMessage}
                />
            ) : null}

            {!isLoading && !errorMessage && settlements.length === 0 ? (
                <EmptyState />
            ) : null}

            {!isLoading && !errorMessage && settlements.length > 0 ? (
                <div className="page-card">
                    <div className="table-wrapper">
                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>정산 코드</th>
                                <th>주문 코드</th>
                                <th>판매자 ID</th>
                                <th>정산 금액</th>
                                <th>상태</th>
                                <th>정산 시각</th>
                                <th>생성 시각</th>
                            </tr>
                            </thead>
                            <tbody>
                            {settlements.map((settlement) => (
                                <tr key={settlement.id}>
                                    <td>
                                        <Link
                                            to={`/admin/settlements/${settlement.id}`}
                                            className="table-link"
                                        >
                                            {settlement.settlementCode}
                                        </Link>
                                    </td>
                                    <td>{settlement.orderCode}</td>
                                    <td>{settlement.sellerId}</td>
                                    <td>{formatPrice(settlement.settlementAmount)}</td>
                                    <td>
                                        <StatusBadge status={settlement.status} />
                                    </td>
                                    <td>{formatDateTime(settlement.settledAt)}</td>
                                    <td>{formatDateTime(settlement.createdAt)}</td>
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