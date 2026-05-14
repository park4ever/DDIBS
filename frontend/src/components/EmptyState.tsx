interface EmptyStateProps {
    title?: string;
    description?: string;
}

export default function EmptyState({
                                       title = "조건에 맞는 결과가 없습니다.",
                                       description = "필터를 조정하거나 검색 범위를 넓혀 다시 확인해보세요.",
                                   }: EmptyStateProps) {
    return (
        <div className="state-box" role="status" aria-live="polite">
            <span className="state-box__eyebrow">조회 안내</span>
            <h3>{title}</h3>
            <p>{description}</p>
        </div>
    );
}