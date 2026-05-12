import { useEffect, useState, type FormEvent } from "react";
import { ApiError } from "../../api/client";
import { getAdminLaunches } from "../../api/launches";
import EmptyState from "../../components/EmptyState";
import LoadingBox from "../../components/LoadingBox";
import PageHeader from "../../components/PageHeader";
import Pagination from "../../components/Pagination";
import StatusBadge from "../../components/StatusBadge";
import type {
    AdminLaunchSearchRequest,
    AdminLaunchSummaryResponse,
    LaunchStatus,
} from "../../types/launch";
import { formatDateTime } from "../../utils/format";

const PAGE_SIZE = 10;

const initialFilterState = {
    launchCode: "",
    status: "",
    sellerId: "",
    productNameKeyword: "",
    from: "",
    to: "",
};

function normalizeText(value: string) {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : undefined;
}

export default function AdminLaunchesPage() {
    const [filters, setFilters] = useState(initialFilterState);
    const [searchCondition, setSearchCondition] =
        useState<AdminLaunchSearchRequest>({});
    const [page, setPage] = useState(0);
    const [launches, setLaunches] = useState<AdminLaunchSummaryResponse[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        void fetchLaunches();
    }, [searchCondition, page]);

    async function fetchLaunches() {
        setIsLoading(true);
        setErrorMessage("");

        try {
            const response = await getAdminLaunches(searchCondition, page, PAGE_SIZE);
            setLaunches(response.content);
            setTotalPages(response.totalPages);
        } catch (error) {
            if (error instanceof ApiError) {
                setErrorMessage(error.message);
            } else {
                setErrorMessage("발매 목록을 불러오는 중 오류가 발생했습니다.");
            }
        } finally {
            setIsLoading(false);
        }
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const nextCondition: AdminLaunchSearchRequest = {
            launchCode: normalizeText(filters.launchCode),
            status: (filters.status as LaunchStatus) || undefined,
            sellerId: normalizeText(filters.sellerId)
                ? Number(filters.sellerId)
                : undefined,
            productNameKeyword: normalizeText(filters.productNameKeyword),
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
                title="발매 / 재고 조회"
                description="발매 상태와 LaunchVariant 재고 현황을 운영 관점에서 확인합니다."
            />

            <div className="page-card">
                <form className="filter-form" onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="정확한 발매 코드"
                        value={filters.launchCode}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                launchCode: event.target.value,
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
                        <option value="UPCOMING">UPCOMING</option>
                        <option value="OPEN">OPEN</option>
                        <option value="CLOSED">CLOSED</option>
                        <option value="ENDED">ENDED</option>
                        <option value="CANCELLED">CANCELLED</option>
                    </select>

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

                    <input
                        type="text"
                        placeholder="상품명"
                        value={filters.productNameKeyword}
                        onChange={(event) =>
                            setFilters((prev) => ({
                                ...prev,
                                productNameKeyword: event.target.value,
                            }))
                        }
                    />

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
                    title="발매 목록을 불러오지 못했습니다."
                    description={errorMessage}
                />
            ) : null}

            {!isLoading && !errorMessage && launches.length === 0 ? (
                <EmptyState />
            ) : null}

            {!isLoading && !errorMessage && launches.length > 0 ? (
                <div className="page-card">
                    <div className="table-wrapper">
                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>발매 코드</th>
                                <th>발매명</th>
                                <th>판매자</th>
                                <th>상품</th>
                                <th>상태</th>
                                <th>발매 시작</th>
                                <th>발매 종료</th>
                                <th>옵션 수</th>
                                <th>총 재고</th>
                                <th>가용 재고</th>
                            </tr>
                            </thead>
                            <tbody>
                            {launches.map((launch) => (
                                <tr key={launch.id}>
                                    <td>{launch.launchCode}</td>
                                    <td>{launch.launchName}</td>
                                    <td>
                                        <div>{launch.sellerName}</div>
                                        <div className="table-subtext">ID: {launch.sellerId}</div>
                                    </td>
                                    <td>{launch.productName}</td>
                                    <td>
                                        <StatusBadge status={launch.status} />
                                    </td>
                                    <td>{formatDateTime(launch.startAt)}</td>
                                    <td>{formatDateTime(launch.endAt)}</td>
                                    <td>{launch.variantCount}</td>
                                    <td>{launch.totalStock}</td>
                                    <td>{launch.availableStock}</td>
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