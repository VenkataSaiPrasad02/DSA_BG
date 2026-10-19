
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

async function request(endpoint, data) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  const result = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(result?.message || "Request failed");
  }

  return result;
}

export const authService = {
  signup: (data) => request("/auth/signup", data),

  verifySignupOtp: (data) =>
    request("/auth/signup/verify-otp", data),

  login: (data) => request("/auth/login", data),

  verifyLoginOtp: (data) =>
    request("/auth/login/verify-otp", data),

  forgotPassword: (data) =>
    request("/auth/forgot-password", data),

  resetPassword: (data) =>
    request("/auth/reset-password", data),
};
