# DSA Battlegrounds - Backend API Inventory and Implementation Plan

## Section 1: Backend API Inventory

### Base Configuration
- **Backend Base URL**: `http://localhost:8080` (configured in frontend axios.jsx)
- **API Gateway**: Port 8080 (routes `/auth/**` to auth-service on 8080)
- **Frontend**: Port 5173 (Vite dev server)

### Authentication Endpoints

#### 1. POST `/auth/register`
- **Purpose**: Initiate user registration and immediately send verification OTP to email
- **Request Body** (`UserRegistrationRequest`):
  ```json
  {
    "username": "string (required)",
    "email": "string (required, @Email validation, unique)",
    "phone": "string (required)",
    "password": "string (required, min 6 characters)"
  }
  ```
- **Response** (`API_Response<String>`):
  ```json
  {
    "message": "Verification OTP sent to your email",
    "data": null
  }
  ```
- **Status**: 200 OK
- **Authentication Required**: No
- **Backend Behavior**: 
  - Validates email uniqueness
  - Hashes password with BCrypt
  - Generates 6-digit OTP
  - Stores pending registration in Redis (5 min TTL)
  - Stores OTP in Redis (2 min TTL)
  - **Immediately sends OTP email via RabbitMQ message queue**
  - If email send fails, Redis entries are deleted and error is thrown

#### 2. POST `/auth/signup/verify-otp`
- **Purpose**: Verify the OTP sent during registration and complete user account creation
- **Request Body** (`SignupOtpVerificationRequest`):
  ```json
  {
    "email": "string (required, @Email, @NotBlank)",
    "otp": "string (required, @NotBlank)"
  }
  ```
- **Response** (`API_Response<String>`):
  ```json
  {
    "message": "Email verified and registration successful",
    "data": null
  }
  ```
- **Status**: 200 OK
- **Authentication Required**: No
- **Backend Behavior**:
  - Retrieves pending registration from Redis
  - Validates OTP from Redis
  - Creates user account with auto-generated ID (format: UU001, UU002, etc.)
  - Sets account status to "ACTIVE"
  - Saves to database
  - Deletes OTP and pending registration from Redis
  - Throws BadCredentialsException if OTP invalid/expired or session expired

#### 3. POST `/auth/login`
- **Purpose**: Initiate login by validating credentials and sending OTP to registered email
- **Request Body** (`UserLoginRequest`):
  ```json
  {
    "login": "string (required, username or email)",
    "password": "string (required)"
  }
  ```
- **Response** (`API_Response<String>`):
  ```json
  {
    "message": "Login OTP sent to your registered email",
    "data": null
  }
  ```
- **Status**: 200 OK
- **Authentication Required**: No
- **Backend Behavior**:
  - Accepts either username or email in `login` field (checks for @ to detect email)
  - Validates password using BCrypt
  - Checks account status is "ACTIVE"
  - Generates 6-digit OTP
  - Stores login OTP in Redis (2 min TTL)
  - **Immediately sends OTP email**
  - Returns success message (does NOT return JWT token yet)

#### 4. POST `/auth/login/verify-otp`
- **Purpose**: Verify login OTP and receive JWT authentication token
- **Request Body** (`LoginOtpRequest`):
  ```json
  {
    "email": "string (required)",
    "otp": "string (required)"
  }
  ```
- **Response** (`AuthResponse`):
  ```json
  {
    "message": "Login successful",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer"
  }
  ```
- **Status**: 200 OK
- **Authentication Required**: No
- **Backend Behavior**:
  - Validates OTP from Redis
  - Checks account status is "ACTIVE"
  - Generates JWT token with claims: sub (userId), email
  - Deletes login OTP from Redis
  - Returns token for frontend storage

#### 5. POST `/auth/forgot-password`
- **Purpose**: Request password reset OTP
- **Request Body** (`ForgotPasswordRequest`):
  ```json
  {
    "email": "string (required)"
  }
  ```
- **Response** (`API_Response<String>`):
  ```json
  {
    "message": "If the account is eligible, password reset instructions will be sent",
    "data": null
  }
  ```
- **Status**: 200 OK
- **Authentication Required**: No
- **Backend Behavior**:
  - Finds user by email
  - Generates 6-digit OTP
  - Stores password reset OTP in Redis (3 min TTL)
  - Sends OTP email
  - Generic message returned for security (doesn't reveal if email exists)

#### 6. POST `/auth/reset-password`
- **Purpose**: Reset password using OTP
- **Request Body** (`ResetPasswordRequest`):
  ```json
  {
    "email": "string (required)",
    "otp": "string (required)",
    "newPassword": "string (required)"
  }
  ```
- **Response** (`API_Response<String>`):
  ```json
  {
    "message": "Password reset successful",
    "data": null
  }
  ```
- **Status**: 200 OK
- **Authentication Required**: No
- **Backend Behavior**:
  - Validates OTP from Redis
  - Finds user by email
  - Hashes new password with BCrypt
  - Updates user password
  - Deletes password reset OTP from Redis

### OAuth2 Social Login Endpoints

#### 7. GET `/oauth2/authorization/{provider}`
- **Purpose**: Initiate OAuth2 login flow
- **Providers**: `google`, `github`, `facebook`
- **Authentication Required**: No
- **Backend Behavior**:
  - Spring Security OAuth2 automatically handles the redirect to provider
  - User authenticates with provider
  - Provider redirects back to auth-service with authorization code

#### 8. OAuth2 Success Handler (Internal)
- **Purpose**: Process successful OAuth2 authentication
- **Redirect URL**: `http://localhost:5173/oauth2/redirect?token={jwt}`
- **Backend Behavior** (`OAuth2AuthenticationSuccessHandler`):
  - Extracts user info from OAuth2User principal
  - Attempts to get userId from `sub` (Google) or `id` (GitHub/Facebook)
  - Extracts email or generates fallback: `{userId}@{provider}.oauth.local`
  - Generates JWT token with userId and email claims
  - Redirects to frontend with token as query parameter

### Protected Endpoints
- **All other endpoints**: Require JWT token in `Authorization: Bearer {token}` header
- **JWT Validation**: Performed by `JwtAuthenticationFilter` before reaching controllers
- **401 Unauthorized**: Frontend axios interceptor catches this and redirects to `/login?error=session_expired`

---

## Section 2: OTP and Email Flow Analysis

### Current OTP Implementation

#### OTP Generation
- **Service**: `OtpGeneratorService` → `OtpGeneratorServiceImpl`
- **Algorithm**: Secure 6-digit random number using `SecureRandom`
- **Format**: Zero-padded 6 digits (e.g., "042387")

#### OTP Storage (Redis)
- **Service**: `OtpRedisService` → `OtpRedisServiceImpl`
- **Storage Keys**:
  - Signup OTP: `dsabg:signup:otp:{email}` (2 min TTL)
  - Login OTP: `dsabg:login:otp:{email}` (2 min TTL)
  - Password Reset OTP: `dsabg:password-reset:otp:{email}` (3 min TTL)
  - Pending Registration: `dsabg:signup:pending:{email}` (5 min TTL, stores username, email, phone, hashedPassword)
- **Email Normalization**: All emails stored as lowercase, trimmed

#### Email Delivery
- **Service**: `OtpEmailService` → Implementation uses RabbitMQ message queue
- **Flow**:
  1. `OtpEmailProducer` publishes OTP message to RabbitMQ queue
  2. `OtpEmailConsumer` (separate consumer service) receives message
  3. Consumer sends actual email via SMTP
- **Configuration**: RabbitMQ on localhost:5672 (guest/guest)
- **Asynchronous**: Email sending is decoupled from API response

### Registration Flow Analysis

✅ **OTP is sent IMMEDIATELY on registration**

When POST `/auth/register` is called:
1. Backend validates request
2. Checks email uniqueness
3. Generates OTP
4. Saves pending registration to Redis
5. Saves OTP to Redis
6. **Calls `otpEmailService.sendOtp(email, otp)` synchronously**
7. If email send fails, Redis entries are deleted and error thrown
8. Returns success response only if OTP was queued/sent

**Conclusion**: The backend ALREADY implements immediate OTP-on-register. No backend changes needed for this requirement.

### Login Flow Analysis

✅ **OTP is sent IMMEDIATELY on login**

When POST `/auth/login` is called:
1. Backend validates credentials (username/email + password)
2. Checks account status is ACTIVE
3. Generates OTP
4. Saves login OTP to Redis
5. **Calls `otpEmailService.sendOtp(email, otp)` synchronously**
6. If email send fails, Redis OTP deleted and error thrown
7. Returns message: "Login OTP sent to your registered email"

**Conclusion**: Login also uses OTP flow. No backend changes needed.

### OTP Expiry Rules
- **Signup OTP**: 2 minutes
- **Login OTP**: 2 minutes
- **Password Reset OTP**: 3 minutes
- **Pending Registration Session**: 5 minutes (includes all registration data)

### Resend OTP Capability
❌ **No dedicated resend endpoint exists**

Current behavior:
- To resend signup OTP: User must call POST `/auth/register` again with same data
- To resend login OTP: User must call POST `/auth/login` again with same credentials
- To resend password reset OTP: User must call POST `/auth/forgot-password` again

**Recommendation**: Add dedicated resend endpoints or document that users should retry the original endpoint.

---

## Section 3: OAuth2 Analysis

### Configured Providers
Based on `application.properties`:

1. **Google OAuth2**
   - Client ID: `${GOOGLE_CLIENT_ID}` (environment variable)
   - Client Secret: `${GOOGLE_CLIENT_SECRET}`
   - Scopes: `openid`, `profile`, `email`
   - User ID claim: `sub`

2. **GitHub OAuth2**
   - Client ID: `${GITHUB_CLIENT_ID}`
   - Client Secret: `${GITHUB_CLIENT_SECRET}`
   - User ID claim: `id`

3. **Facebook OAuth2**
   - Client ID: `${FB_CLIENT_ID}`
   - Client Secret: `${FB_CLIENT_SECRET}`
   - Scopes: `public_profile`, `email`
   - User ID claim: `id`

### Success Handler Behavior

**Class**: `OAuth2AuthenticationSuccessHandler`

**Flow**:
1. Receives OAuth2 authentication from Spring Security
2. Extracts OAuth2User principal
3. Determines provider from OAuth2AuthenticationToken
4. Logs user attributes (with safe JSON serialization for debugging)
5. Extracts user ID:
   - Tries `sub` attribute (Google standard)
   - Falls back to `id` attribute (GitHub/Facebook)
   - Falls back to `getName()` if neither exists
6. Extracts email:
   - Uses `email` attribute if provided
   - Generates fallback: `{userId}@{provider}.oauth.local` if email not available
7. Generates JWT token using `jwtService.generateToken(userId, email)`
8. Redirects to: `http://localhost:5173/oauth2/redirect?token={jwt}`

### Frontend OAuth2RedirectHandler Compatibility

✅ **Perfect match**

Frontend `OAuth2RedirectHandler.jsx`:
1. Reads `token` from query parameter
2. Calls `login(token)` from AuthContext
3. Stores token in localStorage
4. Decodes JWT to extract user info
5. Redirects to `/dashboard`

**Conclusion**: OAuth2 flow is fully implemented and compatible. No changes needed.

### Security Notes
- OAuth2 credentials stored in environment variables (not in code)
- Session management set to `IF_REQUIRED` to support OAuth2 handshake
- JWT tokens used for subsequent authentication (stateless)

---

## Section 4: Naming Standardization Assessment

### Current vs Standard Conventions

#### Endpoint Paths
| Current | Standard/Recommended | Assessment |
|---------|---------------------|------------|
| `/auth/register` | `/auth/signup` or `/auth/register` | ✅ Acceptable |
| `/auth/signup/verify-otp` | `/auth/verify-email` or `/auth/verify-otp` | ⚠️ Inconsistent prefix |
| `/auth/login` | `/auth/signin` or `/auth/login` | ✅ Acceptable |
| `/auth/login/verify-otp` | `/auth/verify-login-otp` | ⚠️ Nested path |

**Recommendation**: Keep current paths for backward compatibility. They are functional though not perfectly RESTful.

#### DTO Field Names
| Endpoint | Current Field | Standard | Assessment |
|----------|--------------|----------|------------|
| Login | `login` | `username` or `email` | ⚠️ Non-standard but functional |
| All | `username` | `username` | ✅ Standard |
| All | `email` | `email` | ✅ Standard |
| All | `password` | `password` | ✅ Standard |

**The `login` field**: Backend accepts username OR email in single field. This is a design choice that trades REST purity for UX convenience. Frontend already uses this correctly.

#### Response Structure
| Type | Current | Standard | Assessment |
|------|---------|----------|------------|
| Success (no data) | `API_Response<String>` with message + null data | Message + data object | ✅ Acceptable |
| Success (with token) | `AuthResponse` with message + token + tokenType | Token + tokenType + expiresIn | ⚠️ Missing expiry info |

**Recommendation**: Consider adding token expiration time to `AuthResponse` in future, but not critical for current implementation.

#### Service/Class Naming
- `AuthController` ✅ Standard
- `AuthService` / `AuthServiceImpl` ✅ Standard Spring pattern
- `UserRegistrationRequest` ✅ Clear and standard
- `SignupOtpVerificationRequest` ✅ Descriptive
- `LoginOtpRequest` ✅ Standard
- `Users` entity ⚠️ Grammatically odd (should be "User") but consistent with DB table name

### Inconsistencies Summary
1. Mixed use of "signup" vs "register" terminology (register endpoint, signup verify-otp path)
2. Login field accepts either username or email (non-standard but intentional UX decision)
3. No explicit token expiration returned (JWT contains exp claim, but not in response)

**Decision**: Accept current naming. Changes would break existing contracts without significant benefit.

---

## Section 5: Frontend Gap Analysis

### What Works Correctly

✅ **Axios Configuration** (`src/api/axios.jsx`)
- Base URL correctly set to backend: `http://localhost:8080`
- JWT token automatically injected in Authorization header
- 401 interceptor redirects to login with session_expired error
- Proper error propagation

✅ **Authentication Context** (`src/context/AuthContext.jsx`)
- JWT token stored in localStorage
- Token decoded to extract user info (userId, email)
- Token expiration checked on initial load
- Login/logout methods provided
- User state management

✅ **Protected Routes** (`src/components/ProtectedRoute.jsx`)
- Checks for user authentication
- Redirects to `/login` if not authenticated
- Wraps protected pages correctly

✅ **OAuth2 Flow** (`src/pages/OAuth2RedirectHandler.jsx`)
- Reads token from query parameter
- Calls login from AuthContext
- Redirects to dashboard
- Error handling for failed auth

✅ **Routing** (`src/App.jsx`)
- Public routes: login, register, oauth2/redirect
- Protected routes: dashboard
- Fallback to login for unknown routes

### What Needs to Be Changed or Added

#### Critical Issues

❌ **Login Flow is Broken**
- **Current**: Login.jsx expects JWT token immediately from POST `/auth/login`
- **Reality**: Backend returns OTP-sent message, NOT a token
- **Required**: Two-step login flow
  1. POST `/auth/login` with credentials → OTP sent message
  2. Show OTP input form
  3. POST `/auth/login/verify-otp` with email + OTP → JWT token
  4. Store token and redirect to dashboard

❌ **Registration Flow is Incomplete**
- **Current**: Register.jsx posts to `/auth/register` then redirects to login after 2 seconds
- **Reality**: Backend sends OTP, user must verify before registration completes
- **Required**: Two-step registration flow
  1. POST `/auth/register` with user details → OTP sent message
  2. Show OTP verification form
  3. POST `/auth/signup/verify-otp` with email + OTP → Registration complete
  4. Show success message and redirect to login

#### Missing Files/Components

❌ **OTP Verification Component**
- **Need**: Dedicated component or page for OTP entry
- **Options**:
  1. Create `VerifyOtp.jsx` page with route
  2. Add inline OTP form to Login/Register pages (conditional rendering)
- **Recommendation**: Use inline forms for better UX (avoid extra navigation)

❌ **Dashboard Layout Components**
- **Missing**:
  - `src/components/layout/Header.jsx`
  - `src/components/layout/Navbar.jsx`
  - `src/components/layout/Aside.jsx`
  - `src/components/layout/DashboardLayout.jsx`
- **Current**: Dashboard.jsx is a simple welcome page without proper layout

❌ **CSS for Layout Components**
- **Missing**:
  - `src/styles/Header.css`
  - `src/styles/Navbar.css`
  - `src/styles/Aside.css`
  - `src/styles/DashboardLayout.css`
- **Existing**: Dashboard.css, Login.css, Register.css, OAuth2RedirectHandler.css

❌ **Forgot Password / Reset Password Pages**
- **Missing**: No UI for forgot-password and reset-password flows
- **Backend Ready**: Endpoints exist and functional
- **Priority**: Medium (not in original MVP but backend supports it)

#### UX Issues

⚠️ **No Loading States**
- Login, Register forms don't show loading indicators during API calls
- Can lead to double-submissions

⚠️ **No Resend OTP Functionality**
- Backend has no dedicated resend endpoint
- Workaround: Retry original request, but UX could be clearer

⚠️ **Limited Error Feedback**
- Generic error messages
- Should display specific backend validation errors

⚠️ **No Password Visibility Toggle**
- Common UX pattern missing
- Mentioned in requirements: "Preserve existing password visibility functionality" - but it doesn't exist yet

#### Route Protection Gaps

⚠️ **Authenticated Users Can Access Login/Register**
- Current: Authenticated users can visit `/login` and `/register`
- Better UX: Redirect authenticated users to `/dashboard` automatically

---

## Section 6: Ordered Implementation Plan

### Phase 1: Fix Authentication Flows (Critical)

#### 1.1 Update Login Page for Two-Step OTP Flow

**File**: `src/pages/Login.jsx`

**Changes**:
1. Add state for tracking flow step: `'credentials' | 'otp' | 'loading'`
2. Add state for storing email after credentials submitted
3. Add state for OTP input value
4. Modify `handleFormSubmit`:
   - POST to `/auth/login` with login + password
   - On success (200), extract email from formData, switch to 'otp' step
   - Display message: "We've sent a verification code to your email"
   - Show error if credentials invalid
5. Add new function `handleOtpSubmit`:
   - POST to `/auth/login/verify-otp` with email + OTP
   - On success, extract token from response.data.token
   - Call login(token) from AuthContext
   - Redirect to `/dashboard`
   - Show error if OTP invalid
6. Add conditional rendering:
   - If step === 'credentials': Show login form
   - If step === 'otp': Show OTP input form with Resend button
7. Add Resend OTP button that re-calls POST `/auth/login`
8. Add loading indicators for both steps
9. Keep social login buttons visible only in 'credentials' step

**CSS Changes**: Update `src/styles/Login.css` to add styles for OTP input form

**Verification**: 
- Manual test: Enter valid credentials → OTP form appears → Enter OTP → Redirect to dashboard
- Manual test: Enter invalid credentials → Error message
- Manual test: Enter invalid OTP → Error message
- Manual test: Click Resend OTP → New OTP sent message

#### 1.2 Update Register Page for Two-Step OTP Flow

**File**: `src/pages/Register.jsx`

**Changes**:
1. Add state for tracking flow step: `'registration' | 'otp'`
2. Add state for storing email after registration submitted
3. Add state for OTP input value
4. Modify `handleFormSubmit`:
   - POST to `/auth/register` with username, email, phone, password
   - On success (200), extract email from formData, switch to 'otp' step
   - Display message: "We've sent a verification code to {email}"
   - Show error if email already exists or validation fails
5. Add new function `handleOtpSubmit`:
   - POST to `/auth/signup/verify-otp` with email + OTP
   - On success (200), show success message
   - Redirect to `/login` after 2 seconds
   - Show error if OTP invalid or expired
6. Add conditional rendering:
   - If step === 'registration': Show registration form
   - If step === 'otp': Show OTP input form with Resend button
7. Add Resend OTP button that re-calls POST `/auth/register` with original formData
8. Add loading indicators for both steps
9. Handle session expiration error gracefully (tell user to start over)

**CSS Changes**: Update `src/styles/Register.css` to add styles for OTP input form

**Verification**:
- Manual test: Enter registration details → OTP form appears → Enter valid OTP → Success message → Redirect to login
- Manual test: Enter existing email → Error message
- Manual test: Enter invalid OTP → Error message
- Manual test: Click Resend OTP → New OTP sent message
- Manual test: Wait 5 minutes then verify OTP → Session expired error

#### 1.3 Add Redirect Logic for Authenticated Users

**Files**: `src/pages/Login.jsx`, `src/pages/Register.jsx`

**Changes**:
1. Import `useAuth` hook
2. Import `useEffect` and `useNavigate`
3. Add useEffect that checks if user is authenticated
4. If user exists, redirect to `/dashboard`

**Code snippet for both files**:
```jsx
const { user } = useAuth();

useEffect(() => {
  if (user) {
    navigate('/dashboard', { replace: true });
  }
}, [user, navigate]);
```

**Verification**: 
- Manual test: Login successfully → Try to visit /login → Auto-redirect to /dashboard
- Manual test: Login successfully → Try to visit /register → Auto-redirect to /dashboard

### Phase 2: Create Dashboard Layout Components

#### 2.1 Create Header Component

**File**: `src/components/layout/Header.jsx`

**Content**:
- Display "DSA Battlegrounds" logo/title
- Show authenticated user email on the right
- Logout button that calls logout() from AuthContext and redirects to /login
- Responsive design (hamburger menu for mobile if needed)

**File**: `src/styles/Header.css`

**Styling**:
- Horizontal layout
- Fixed or sticky positioning
- Primary brand colors
- Hover effects on logout button

**Verification**: 
- Import and render in Dashboard → Header appears with user email
- Click logout → Token cleared, redirect to login

#### 2.2 Create Navbar Component

**File**: `src/components/layout/Navbar.jsx`

**Content**:
- Navigation links using React Router Link component:
  - Dashboard (Home)
  - Problems (placeholder)
  - Leaderboard (placeholder)
  - Profile (placeholder)
- Highlight active route using useLocation
- Responsive design

**File**: `src/styles/Navbar.css`

**Styling**:
- Horizontal nav bar (desktop) or vertical sidebar (mobile)
- Active link styling
- Hover effects

**Verification**:
- Navigate between routes → Active link highlighted
- All links work without full page reload

#### 2.3 Create Aside (Sidebar) Component

**File**: `src/components/layout/Aside.jsx`

**Content**:
- Sidebar navigation for Dashboard-specific sections:
  - Overview
  - My Submissions
  - Progress
  - Settings
- Collapsible on mobile
- Optional close/expand toggle

**File**: `src/styles/Aside.css`

**Styling**:
- Vertical sidebar
- Fixed width on desktop, collapsible on mobile
- Background color distinct from main content

**Verification**:
- Render in Dashboard → Aside appears on left side
- Resize window → Responsive behavior works

#### 2.4 Create DashboardLayout Component

**File**: `src/components/layout/DashboardLayout.jsx`

**Content**:
- Wrapper component that composes Header, Navbar, Aside, and children (main content)
- Flexible layout: Header at top, Navbar below/beside header, Aside on left, content in center
- Uses React children prop to render page content

**Structure**:
```jsx
<div className="dashboard-layout">
  <Header />
  <div className="dashboard-body">
    <Navbar />
    <div className="dashboard-main">
      <Aside />
      <main className="dashboard-content">
        {children}
      </main>
    </div>
  </div>
</div>
```

**File**: `src/styles/DashboardLayout.css`

**Styling**:
- CSS Grid or Flexbox layout
- Responsive breakpoints
- Proper spacing and overflow handling

**Verification**:
- Wrap Dashboard page content with DashboardLayout → Full layout renders correctly
- Test on desktop and mobile viewports

#### 2.5 Update Dashboard Page to Use Layout

**File**: `src/pages/Dashboard.jsx`

**Changes**:
1. Import DashboardLayout
2. Remove redundant header/logout code (now in Header component)
3. Wrap content with DashboardLayout:
```jsx
return (
  <DashboardLayout>
    <div className="dashboard-welcome">
      <h1>Welcome to DSA Battlegrounds</h1>
      <p>User ID: {user.userId}</p>
      <p>Email: {user.email}</p>
    </div>
  </DashboardLayout>
);
```
4. Update Dashboard.css to style only the welcome content area

**Verification**:
- Visit /dashboard → See Header, Navbar, Aside, and welcome content
- All layout components work together
- No duplicate logout buttons or headers

### Phase 3: Enhance UX and Polish

#### 3.1 Add Loading States

**Files**: `src/pages/Login.jsx`, `src/pages/Register.jsx`

**Changes**:
1. Add `isLoading` state variable (boolean)
2. Set `isLoading = true` when submitting credentials or OTP
3. Set `isLoading = false` when response received (success or error)
4. Disable submit button when loading
5. Show loading text or spinner in button: "Sending..." / "Verifying..."

**Verification**:
- Submit form → Button disabled and shows loading state
- After response → Button re-enabled

#### 3.2 Add Password Visibility Toggle

**Files**: `src/pages/Login.jsx`, `src/pages/Register.jsx`

**Changes**:
1. Add state: `showPassword` (boolean, default false)
2. Change input type dynamically: `type={showPassword ? 'text' : 'password'}`
3. Add eye icon button next to password input
4. Toggle `showPassword` on button click

**CSS**: Add styles for eye icon button (position absolute, inside input field right side)

**Verification**:
- Click eye icon → Password becomes visible
- Click again → Password hidden

#### 3.3 Improve Error Messaging

**Files**: `src/pages/Login.jsx`, `src/pages/Register.jsx`

**Changes**:
1. Parse backend error responses more intelligently
2. Display specific field validation errors if available
3. Handle common error scenarios:
   - Invalid credentials
   - Email already exists
   - OTP expired
   - Session expired (registration pending > 5 min)
   - Network errors
4. Show errors prominently in red alert box

**Verification**:
- Trigger each error type → Appropriate message displayed

#### 3.4 Add Resend OTP with Cooldown

**Files**: `src/pages/Login.jsx`, `src/pages/Register.jsx`

**Changes**:
1. Add state: `resendCooldown` (number, seconds remaining)
2. Resend OTP button disabled during cooldown
3. Start 30-second countdown after sending OTP
4. Button text changes: "Resend OTP (30s)" → "Resend OTP"
5. Clear interval on component unmount

**Verification**:
- Request OTP → Resend button disabled for 30 seconds
- After 30 seconds → Resend button enabled
- Click Resend → New OTP sent, cooldown resets

### Phase 4: Optional Enhancements (If Time Permits)

#### 4.1 Create Forgot Password Page

**File**: `src/pages/ForgotPassword.jsx`

**Flow**:
1. Input email field
2. POST `/auth/forgot-password` → OTP sent message
3. Show OTP input form
4. Show new password input field
5. POST `/auth/reset-password` with email, OTP, newPassword
6. Success → Redirect to login

**Route**: Add `/forgot-password` to App.jsx

**Verification**: Full forgot password flow works

#### 4.2 Add "Forgot Password?" Link to Login

**File**: `src/pages/Login.jsx`

**Changes**: Add link below password field that navigates to `/forgot-password`

#### 4.3 Add Token Expiration Warning

**File**: `src/context/AuthContext.jsx`

**Changes**:
1. Check JWT exp claim
2. Set timer to warn user 5 minutes before expiration
3. Show toast/modal: "Your session will expire soon. Please save your work."

#### 4.4 Add Placeholder Pages for Navigation

**Files**: 
- `src/pages/Problems.jsx`
- `src/pages/Leaderboard.jsx`
- `src/pages/Profile.jsx`

**Content**: Simple placeholder: "Coming soon" message

**Routes**: Add to App.jsx as protected routes

---

## Section 7: Decisions and Constraints

### Design Decisions

1. **OTP Input Placement**: Use inline forms instead of separate routes
   - **Rationale**: Better UX, less navigation, maintains context
   - **Alternative**: Separate `/verify-otp` page with email passed as state
   - **Decision**: Inline forms with conditional rendering

2. **Resend OTP Strategy**: Call original endpoint again (no dedicated resend endpoint)
   - **Rationale**: Backend has no separate resend endpoint
   - **Impact**: User must re-enter credentials for login OTP resend
   - **Recommendation**: Keep form data in state to simplify resends

3. **Dashboard Layout Structure**: Header + Navbar + Aside + Content
   - **Rationale**: Separate concerns, reusable components
   - **Alternative**: Single monolithic Dashboard component
   - **Decision**: Modular components in `src/components/layout/`

4. **No Footer**: Per requirements, application has no footer
   - **Rationale**: User explicitly requested no footer
   - **Decision**: Header, Navbar, Aside, and content area only

5. **Error Messages**: Display backend error messages directly
   - **Rationale**: Backend already provides user-friendly messages
   - **Alternative**: Map error codes to frontend messages
   - **Decision**: Use backend messages as-is, with fallbacks for network errors

### Potential User-Visible Behavior Changes

⚠️ **Login now requires OTP verification**
- **Current**: Users might expect immediate login (if they haven't used the system yet)
- **New**: Users must check email and enter OTP code
- **Impact**: Slightly longer login process, but more secure
- **Mitigation**: Clear messaging about OTP email

⚠️ **Registration requires email verification before login**
- **Current**: Registration shows "success" immediately
- **New**: Registration requires OTP verification step before account is created
- **Impact**: Users must complete verification to create account
- **Mitigation**: Clear instructions, resend OTP option

### External Dependencies

1. **RabbitMQ**: Must be running on localhost:5672 for OTP emails to send
   - **Impact**: If RabbitMQ is down, OTP emails fail, registration/login blocked
   - **Recommendation**: Verify RabbitMQ is running before testing

2. **Redis**: Must be running for OTP and session storage
   - **Impact**: If Redis is down, OTP verification fails
   - **Recommendation**: Verify Redis is running before testing

3. **SMTP Service**: Email consumer must be configured with valid SMTP credentials
   - **Impact**: Without SMTP config, OTPs won't be delivered (but queued in RabbitMQ)
   - **Recommendation**: Verify email service is configured and test with real email

4. **OAuth2 Credentials**: Environment variables must be set for social login
   - **Variables**: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `FB_CLIENT_ID`, `FB_CLIENT_SECRET`
   - **Impact**: Social login buttons won't work without valid credentials
   - **Recommendation**: Document setup or hide buttons if not configured

5. **Database**: MySQL/PostgreSQL must be running for user persistence
   - **Impact**: Registration and login will fail if database is unavailable
   - **Recommendation**: Verify database connection in backend logs

### Security Considerations

1. **JWT Secrets**: Backend must have strong JWT signing secret configured
2. **Password Hashing**: Already using BCrypt (secure)
3. **OTP Exposure**: OTPs are sent via email, not exposed in API responses (secure)
4. **Token Storage**: Using localStorage (acceptable for MVP, consider httpOnly cookies for production)
5. **CORS**: Backend has `@CrossOrigin("*")` - should restrict to frontend domain in production

### Testing Requirements

**Manual Testing Checklist**:
- [ ] Register new user → Receive OTP email → Verify OTP → Account created
- [ ] Login with credentials → Receive OTP email → Verify OTP → Redirect to dashboard
- [ ] Resend OTP during registration
- [ ] Resend OTP during login
- [ ] Invalid OTP error handling
- [ ] Expired OTP error handling (wait > 2 minutes)
- [ ] Duplicate email registration error
- [ ] Invalid credentials login error
- [ ] OAuth2 login with Google
- [ ] OAuth2 login with GitHub
- [ ] OAuth2 login with Facebook
- [ ] Protected route access without authentication → Redirect to login
- [ ] Authenticated user visits /login → Redirect to dashboard
- [ ] Logout → Token cleared, redirect to login, can't access dashboard
- [ ] Session expiration handling (manual token deletion)
- [ ] Dashboard layout renders correctly on desktop
- [ ] Dashboard layout renders correctly on mobile
- [ ] Password visibility toggle works
- [ ] Loading indicators appear during API calls

**Prerequisites for Testing**:
1. Backend auth-service running on port 8080
2. RabbitMQ running on port 5672
3. Redis running on default port
4. Email consumer service running and configured with SMTP
5. Database running and migrated
6. Frontend running on port 5173
7. At least one OAuth2 provider configured (Google recommended)

---

## Implementation Order Summary

1. **Fix Login OTP flow** (Critical)
2. **Fix Register OTP flow** (Critical)
3. **Add authenticated user redirects** (Critical)
4. **Create Header component** (High priority)
5. **Create Navbar component** (High priority)
6. **Create Aside component** (High priority)
7. **Create DashboardLayout component** (High priority)
8. **Update Dashboard to use layout** (High priority)
9. **Add loading states** (Medium priority)
10. **Add password visibility toggle** (Medium priority)
11. **Improve error messaging** (Medium priority)
12. **Add resend OTP with cooldown** (Medium priority)
13. **Create Forgot Password page** (Optional)
14. **Add token expiration warning** (Optional)
15. **Add placeholder navigation pages** (Optional)

**Estimated Effort**:
- Critical fixes (1-3): 3-4 hours
- Dashboard layout (4-8): 3-4 hours
- UX polish (9-12): 2-3 hours
- Optional features (13-15): 2-3 hours
- **Total**: 10-14 hours for complete implementation

---

## Backend Changes Required

✅ **ZERO backend changes required for core functionality**

The backend already:
- ✅ Sends OTP immediately on registration
- ✅ Sends OTP immediately on login
- ✅ Validates OTPs correctly
- ✅ Generates secure JWT tokens
- ✅ Supports OAuth2 social login
- ✅ Provides proper error messages
- ✅ Has password reset functionality

**Optional Backend Enhancement** (not blocking):
- Add dedicated resend OTP endpoints (lower priority, can workaround by re-calling original endpoints)

---

## Final Notes

This implementation plan is grounded in actual code inspection of both backend and frontend. All API endpoints, request/response formats, and flows are documented based on actual Java controller methods, DTOs, and service implementations.

The frontend currently has a partially working authentication system that needs to be updated to match the backend's two-step OTP flow. The dashboard exists but lacks proper layout components.

No new backend development is required. All work is frontend implementation to match the existing backend API contracts.

The OAuth2 flow is already fully functional and requires no changes.
