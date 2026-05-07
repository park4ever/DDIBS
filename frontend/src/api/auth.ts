import type { LoginRequest, LoginResponse, MeResponse } from "../types/auth";
import { apiRequest } from "./client";

interface LogoutResponse {
    message: string;
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
    return apiRequest<LoginResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export async function getMe(): Promise<MeResponse> {
    return apiRequest<MeResponse>("/api/auth/me", {
        method: "GET",
    });
}

export async function logout(): Promise<LogoutResponse> {
    return apiRequest<LogoutResponse>("/api/auth/logout", {
        method: "POST",
    });
}