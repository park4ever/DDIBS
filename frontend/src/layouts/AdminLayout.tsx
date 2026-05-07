import { Link, Outlet, useNavigate } from "react-router";
import { ApiError } from "../api/client";
import { useAuth } from "../contexts/AuthContext";

export default function AdminLayout() {
    const navigate = useNavigate();
    const { user, signOut } = useAuth();

    async function handleLogout() {
        try {
            await signOut();
            navigate("/login", { replace: true });
        } catch (error) {
            if (error instanceof ApiError) {
                alert(error.message);
                return;
            }

            alert("로그아웃 중 오류가 발생했습니다.");
        }
    }

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar">
                <div className="sidebar-brand">
                    <h1>DDIBS</h1>
                    <p>Admin Console</p>
                </div>

                <nav className="sidebar-nav">
                    <Link to="/admin/orders">주문 조회</Link>
                    <Link to="/admin/settlements">정산 조회</Link>
                    <Link to="/admin/launches">발매 / 재고 조회</Link>
                </nav>
            </aside>

            <div className="admin-main">
                <header className="admin-header">
                    <div>
                        <strong>운영 대시보드</strong>
                        {user ? (
                            <div className="admin-user-meta">
                                {user.name} · {user.role}
                            </div>
                        ) : null}
                    </div>

                    <button type="button" onClick={handleLogout}>
                        로그아웃
                    </button>
                </header>

                <main className="admin-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}