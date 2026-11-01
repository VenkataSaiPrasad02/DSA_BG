import { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useParams } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

import Login from './pages/Login';
import Register from './pages/Register';
import VerifyOtp from './pages/VerifyOtp';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Dashboard from './pages/Dashboard';
import OAuth2RedirectHandler from './pages/OAuth2RedirectHandler';
import Profile from './pages/Profile';
import Friends from './pages/Friends';
import FriendRequests from './pages/FriendRequests';
import FriendsList from './pages/FriendsList';
import BattleLobby from './pages/BattleLobby';
import CustomRooms from './pages/CustomRooms';
import Practice from './pages/Practice';
import PracticeCategory from './pages/PracticeCategory';
import PracticeProblems from './pages/PracticeProblems';
import ProblemDetail from './pages/ProblemDetail';
import ProtectedRoute from './components/ProtectedRoute';
import DashboardLayout from './components/layout/DashboardLayout';

const BattleWorkspace = lazy(() => import('./pages/BattleWorkspace'));
const ProblemCodingWorkspace = lazy(() => import('./pages/ProblemCodingWorkspace'));

const AppEntryRedirect = () => {
    const { user } = useAuth();
    return <Navigate to={user ? '/dashboard' : '/login'} replace />;
};

const LegacyPracticeCategoryRedirect = () => {
    const { category } = useParams();
    return (
        <Navigate
            to={`/practice/categories/${encodeURIComponent(category)}`}
            replace
        />
    );
};

function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path="/" element={<AppEntryRedirect />} />

                    {/* ── Public routes ──────────────────────────────────── */}
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/verify-otp" element={<VerifyOtp />} />
                    <Route path="/forgot-password" element={<ForgotPassword />} />
                    <Route path="/reset-password" element={<ResetPassword />} />
                    <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />

                    {/* ── Protected routes (require valid JWT) ───────────── */}
                    <Route
                        element={
                            <ProtectedRoute>
                                <DashboardLayout />
                            </ProtectedRoute>
                        }
                    >
                        <Route path="/dashboard" element={<Dashboard />} />
                        <Route path="/profile" element={<Profile />} />
                        <Route path="/profile/:userId" element={<Profile />} />
                        <Route path="/friends" element={<Friends />} />
                        <Route path="/friends/requests" element={<FriendRequests />} />
                        <Route path="/friends/list" element={<FriendsList />} />
                        <Route path="/battles" element={<BattleLobby />} />
                        <Route path="/custom-rooms" element={<CustomRooms />} />
                        <Route path="/practice" element={<Practice />} />
                        <Route path="/practice/categories/:category" element={<PracticeCategory />} />
                        <Route path="/practice/categories/:category/problems" element={<PracticeProblems />} />
                        <Route path="/practice/categories/:category/patterns/:patternSlug" element={<PracticeProblems />} />
                        <Route path="/practice/patterns/:patternSlug" element={<PracticeProblems />} />
                        <Route path="/practice/:category" element={<LegacyPracticeCategoryRedirect />} />
                        <Route path="/problems" element={<PracticeProblems allProblems />} />
                        <Route path="/problems/:slug" element={<ProblemDetail />} />
                    </Route>

                    <Route
                        path="/problems/:slug/solve"
                        element={
                            <ProtectedRoute>
                                <Suspense fallback={<div role="status">Loading coding workspace…</div>}>
                                    <ProblemCodingWorkspace />
                                </Suspense>
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/custom-rooms/:roomCode"
                        element={
                            <ProtectedRoute>
                                <CustomRooms />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/battle-workspace/:roomCode"
                        element={
                            <ProtectedRoute>
                                <Suspense fallback={<div role="status">Loading battle workspace…</div>}>
                                    <BattleWorkspace />
                                </Suspense>
                            </ProtectedRoute>
                        }
                    />

                    {/* ── Catch-all ──────────────────────────────────────── */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;
