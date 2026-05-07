export class ApiError extends Error {
    status: number;

    constructor(status: number, message: string) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

export async function apiRequest<T>(
    path: string,
    options: RequestInit = {},
): Promise<T> {
    const headers = new Headers(options.headers);

    const isFormData = options.body instanceof FormData;

    if (!isFormData && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    const response = await fetch(path, {
        ...options,
        headers,
        credentials: "include",
    });

    const contentType = response.headers.get("Content-Type") ?? "";
    const isJson = contentType.includes("application/json");

    const data = isJson ? await response.json() : null;

    if (!response.ok) {
        const message =
            data?.message ??
            data?.error ??
            "요청 처리 중 오류가 발생했습니다.";

        throw new ApiError(response.status, message);
    }

    return data as T;
}