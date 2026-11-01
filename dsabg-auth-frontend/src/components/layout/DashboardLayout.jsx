import { Outlet } from 'react-router-dom';
import Header from './Header';
import Navbar from './Navbar';
import Aside from './Aside';
import '../../styles/DashboardLayout.css';

/**
 * Shell layout for all authenticated dashboard pages.
 * Structure:
 *   <header>   — branding + user + logout
 *   <nav>      — main top navigation
 *   <div.body>
 *     <aside>  — sidebar
 *     <main>   — page content via <Outlet />
 *   </div>
 * No footer.
 */
const DashboardLayout = () => {
    return (
        <div className="dashboard-layout">
            <Header />
            <Navbar />

            <div className="dashboard-body">
                <Aside />
                <main className="dashboard-main" id="main-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
};

export default DashboardLayout;
