import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router";
import { ApiError } from "../../api/client";
import { useAuth } from "../../contexts/AuthContext";

export default function LoginPage() {
    const navigate = useNavigate();
    const { signIn, isInitializing, isAuthenticated } = useAuth();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setErrorMessage("");
        setIsSubmitting(true);

        try {
            await signIn({ email, password });
            navigate("/admin/orders", { replace: true });
        } catch (error) {
            if (error instanceof ApiError) {
                setErrorMessage(error.message);
            } else {
                setErrorMessage("로그인 중 오류가 발생했습니다.");
            }
        } finally {
            setIsSubmitting(false);
        }
    }

    if (isInitializing) {
        return (
            <div className="login-page">
                <div className="login-card">
                    <h1>DDIBS Admin</h1>
                    <p>인증 상태를 확인하는 중입니다...</p>
                </div>
            </div>
        );
    }

    if (isAuthenticated) {
        return (
            <div className="login-page">
                <div className="login-card">
                    <h1>DDIBS Admin</h1>
                    <p>이미 로그인된 상태입니다.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="login-page">
            <div className="login-card">
                <h1>DDIBS Admin</h1>
                <p>고수요 발매 운영 시스템 관리자 로그인</p>

                <form className="login-form" onSubmit={handleSubmit}>
                    <label>
                        이메일
                        <input
                            type="email"
                            placeholder="admin@ddibs.com"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            autoComplete="username"
                        />
                    </label>

                    <label>
                        비밀번호
                        <input
                            type="password"
                            placeholder="비밀번호를 입력하세요"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            autoComplete="current-password"
                        />
                    </label>

                    {errorMessage ? <p className="form-error">{errorMessage}</p> : null}

                    <button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "로그인 중..." : "로그인"}
                    </button>
                </form>
            </div>
        </div>
    );
}