import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/OAuth2RedirectHandler.css';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');

    if (token) {
      login(token);
      navigate('/dashboard');
    } else {
      navigate('/login?error=auth_failed');
    }
  }, [location, navigate, login]);

  return (
    <div className="oauth-loading-container">
      <h3>Authenticating your session, please wait...</h3>
    </div>
  );
};

export default OAuth2RedirectHandler;
