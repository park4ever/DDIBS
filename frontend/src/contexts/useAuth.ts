import { createContext, useContext } from "react";
import type { LoginRequest, MeResponse } from "../types/auth";

interface AuthContextValue {
    user: MeResponse | null;
    isAuthenticated: boolean;
    isInitializing: boolean;
    signIn: (request: LoginRequest) => Promise<void>;
    signOut: () => Promise<void>;
    refreshMe: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }

    return context;
}