import { useEffect, useMemo, useState, type ReactNode } from "react";
import { getMe, login, logout } from "../api/auth";
import type { LoginRequest, MeResponse } from "../types/auth";
import { AuthContext } from "./useAuth";

interface AuthContextValue {
    user: MeResponse | null;
    isAuthenticated: boolean;
    isInitializing: boolean;
    signIn: (request: LoginRequest) => Promise<void>;
    signOut: () => Promise<void>;
    refreshMe: () => Promise<void>;
}

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [user, setUser] = useState<MeResponse | null>(null);
    const [isInitializing, setIsInitializing] = useState(true);

    useEffect(() => {
        let cancelled = false;

        getMe()
            .then((me) => {
                if (cancelled) {
                    return;
                }

                setUser(me);
            })
            .catch(() => {
                if (cancelled) {
                    return;
                }

                setUser(null);
            })
            .finally(() => {
                if (!cancelled) {
                    setIsInitializing(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, []);

    async function signIn(request: LoginRequest) {
        const response = await login(request);

        setUser({
            id: response.id,
            email: response.email,
            name: response.name,
            role: response.role,
        });
    }

    async function signOut() {
        await logout();
        setUser(null);
    }

    async function refreshMe() {
        const me = await getMe();
        setUser(me);
    }

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            isAuthenticated: user !== null,
            isInitializing,
            signIn,
            signOut,
            refreshMe,
        }),
        [user, isInitializing],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}