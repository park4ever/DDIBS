interface StatusBadgeProps {
    status: string;
}

function getStatusClassName(status: string) {
    switch (status) {
        case "CONFIRMED":
        case "SUCCESS":
        case "OPEN":
            return "status-badge status-badge--success";

        case "CREATED":
        case "PENDING":
        case "UPCOMING":
            return "status-badge status-badge--pending";

        case "PAYMENT_FAILED":
        case "FAILED":
        case "CANCELLED":
            return "status-badge status-badge--danger";

        case "HOLD_EXPIRED":
        case "EXPIRED":
        case "CLOSED":
        case "ENDED":
            return "status-badge status-badge--neutral";

        default:
            return "status-badge";
    }
}

export default function StatusBadge({ status }: StatusBadgeProps) {
    return <span className={getStatusClassName(status)}>{status}</span>;
}