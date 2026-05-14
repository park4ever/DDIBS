interface LoadingBoxProps {
    title?: string;
    message?: string;
}

export default function LoadingBox({
                                       title = "데이터를 불러오는 중입니다.",
                                       message = "잠시만 기다려주세요.",
                                   }: LoadingBoxProps) {
    return (
        <div className="state-box" role="status" aria-live="polite">
            <span className="state-box__eyebrow">로딩 중</span>
            <h3>{title}</h3>
            <p>{message}</p>
        </div>
    );
}