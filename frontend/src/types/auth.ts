export type Role = "USER" | "ADMIN";

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    id: number;
    email: string;
    name: string;
    role: Role;
}

export interface MeResponse {
    id: number;
    email: string;
    name: string;
    role: Role;
}