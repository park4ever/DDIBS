import type { ReactNode } from "react";
import { Navigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";

interface ProtectedRouteProps {
    children: ReactNode;
}

export default function ProtectedRoute({
                                           children,
                                       }: ProtectedRouteProps) {
    const { isAuthenticated, isInitializing } = useAuth();

    if (isInitializing) {
        return <div style={{ padding: "24px" }}>인증 상태를 확인하는 중입니다...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
}