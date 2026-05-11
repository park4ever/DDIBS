interface LoadingBoxProps {
    message?: string;
}

export default function LoadingBox({
                                       message = "데이터를 불러오는 중입니다...",
                                   }: LoadingBoxProps) {
    return (
        <div className="state-box">
            <p>{message}</p>
        </div>
    );
}