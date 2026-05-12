export function formatDateTime(value: string | null) {
    if (!value) {
        return "-";
    }

    return new Date(value).toLocaleString();
}

export function formatPrice(value: number) {
    return `${Number(value).toLocaleString()}원`;
}