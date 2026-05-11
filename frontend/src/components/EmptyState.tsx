interface EmptyStateProps {
    title?: string;
    description?: string;
}

export default function EmptyState({
                                       title = "조회 결과가 없습니다.",
                                       description = "검색 조건을 변경해서 다시 시도해보세요.",
                                   }: EmptyStateProps) {
    return (
        <div className="state-box">
            <h3>{title}</h3>
            <p>{description}</p>
        </div>
    );
}