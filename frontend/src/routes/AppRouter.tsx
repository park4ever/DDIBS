import { Navigate, createBrowserRouter, RouterProvider } from "react-router";
import AdminLayout from "../layouts/AdminLayout";
import { useAuth } from "../contexts/AuthContext";
import AdminOrdersPage from "../pages/admin-orders/AdminOrdersPage";
import AdminLaunchesPage from "../pages/admin-launches/AdminLaunchesPage";
import AdminSettlementsPage from "../pages/admin-settlements/AdminSettlementsPage";
import LoginPage from "../pages/login/LoginPage";
import ProtectedRoute from "./ProtectedRoute";

function LoginRedirectRoute() {
    const { isAuthenticated, isInitializing } = useAuth();

    if (isInitializing) {
        return <div style={{ padding: "24px" }}>인증 상태를 확인하는 중입니다...</div>;
    }

    if (isAuthenticated) {
        return <Navigate to="/admin/orders" replace />;
    }

    return <LoginPage />;
}

const router = createBrowserRouter([
    {
        path: "/",
        element: <Navigate to="/login" replace />,
    },
    {
        path: "/login",
        element: <LoginRedirectRoute />,
    },
    {
        path: "/admin",
        element: (
            <ProtectedRoute>
                <AdminLayout />
            </ProtectedRoute>
        ),
        children: [
            {
                path: "orders",
                element: <AdminOrdersPage />,
            },
            {
                path: "settlements",
                element: <AdminSettlementsPage />,
            },
            {
                path: "launches",
                element: <AdminLaunchesPage />,
            },
        ],
    },
]);

export default function AppRouter() {
    return <RouterProvider router={router} />;
}