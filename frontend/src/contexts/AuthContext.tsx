import {
    createContext,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from "react";
import { getMe, login, logout } from "../api/auth";
import type { LoginRequest, MeResponse } from "../types/auth";

interface AuthContextValue {
    user: MeResponse | null;
    isAuthenticated: boolean;
    isInitializing: boolean;
    signIn: (request: LoginRequest) => Promise<void>;
    signOut: () => Promise<void>;
    refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [user, setUser] = useState<MeResponse | null>(null);
    const [isInitializing, setIsInitializing] = useState(true);

    useEffect(() => {
        void initializeAuth();
    }, []);

    async function initializeAuth() {
        try {
            const me = await getMe();
            setUser(me);
        } catch {
            setUser(null);
        } finally {
            setIsInitializing(false);
        }
    }

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

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }

    return context;
}