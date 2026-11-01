
import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

const decodeTokenClaims = (token) => {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return null;

        const base64Url = parts[1];
        const base64 = base64Url
            .replace(/-/g, '+')
            .replace(/_/g, '/');

        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(
                    (c) =>
                        '%' +
                        ('00' + c.charCodeAt(0).toString(16)).slice(-2)
                )
                .join('')
        );

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Token decoding failed:', e);
        return null;
    }
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(() => {
        // Initialize user from token on first render
        const token = localStorage.getItem('token');

        if (token) {
            const claims = decodeTokenClaims(token);

            if (claims && claims.exp * 1000 > Date.now()) {
                return {
                    userId: claims.sub,
                    userName: claims.username || claims.userName,
                    email: claims.email,
                };
            }

            localStorage.removeItem('token');
        }

        return null;
    });

    const login = (token) => {
        localStorage.setItem('token', token);

        const claims = decodeTokenClaims(token);

        if (claims) {
            setUser({
                userId: claims.sub,
                userName: claims.username || claims.userName,
                email: claims.email,
            });
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    return (
        <AuthContext.Provider
            value={{ user, loading: false, login, logout }}
        >
            {children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
