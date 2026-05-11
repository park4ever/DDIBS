interface PaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

export default function Pagination({
                                       page,
                                       totalPages,
                                       onPageChange,
                                   }: PaginationProps) {
    if (totalPages <= 1) {
        return null;
    }

    return (
        <div className="pagination">
            <button
                type="button"
                onClick={() => onPageChange(page - 1)}
                disabled={page === 0}
            >
                이전
            </button>

            <span>
        {page + 1} / {totalPages}
      </span>

            <button
                type="button"
                onClick={() => onPageChange(page + 1)}
                disabled={page >= totalPages - 1}
            >
                다음
            </button>
        </div>
    );
}