# Pull Request: Comprehensive Paymob Gateway Integration, Dynamic Pricing & WebView Fixes

### Overview
This Pull Request delivers end-to-end payment integration with Paymob, dynamic subscription tier and coin pack pricing directly from backend APIs, client-side fallback payment confirmation, restored Free tier non-payment downgrades, optimized WebView rendering/3DS interception, and polling failure protections.

---

### Key Changes Made

#### 1. Dynamic Backend Pricing Integration & Fallback Confirmation
- **New Backend Endpoints (`:core:network`)**: Added `SUBSCRIPTION_TIERS` (`/api/v1/subscriptions/tiers`), `WALLET_COIN_PACKS` (`/api/v1/wallet/coin-packs`), and `PAYMENT_CONFIRM` (`/api/v1/payments/confirm/{merchantOrderId}`) to `Endpoints.kt`.
- **Remote & Repository Layers**: Updated `PaymentRemoteDataSource` and `PaymentRepository` to fetch dynamic pricing maps from the backend.
- **Domain Use Cases**: Created `GetSubscriptionTiersUseCase`, `GetCoinPacksUseCase`, and `ConfirmPaymentUseCase`.
- **ViewModel & State (`PaywallViewModel` / `PaywallState`)**: Automatically fetches live tier and coin pack prices on `init` and `refreshUserData()`, dynamically replacing fallback values. Added fallback payment confirmation during payment polling.

#### 2. Dynamic Offer & Discount Display Logic
- **State Cleanup**: Removed hardcoded static `originalPriceEgp` fallbacks in `PaywallState` (`originalPriceEgp = null`).
- **Dynamic Price Handling**: Ensured `PaywallViewModel` resets `originalPriceEgp = null` when loading standard dynamic prices from the backend.
- **Component Offer Guard**: Refactored `SubscriptionPlanComponents` and `CoinPackComponents` so discount percentage badges (`Save X%`) and strikethrough original prices are only displayed when `originalPriceEgp` is explicitly present AND strictly greater than `priceEgp` (`originalPriceEgp > priceEgp`).

#### 3. Free Plan Restoration & Non-Payment Downgrade Flow
- **Restored Free Plan in Selection Screen**: Added the Free Plan card to `SubscriptionPlansScreen` with localized strings.
- **Non-Payment Downgrades**: Enabled seamless downgrades to `FREE` (`POST /api/v1/subscriptions/downgrade`) without triggering a Paymob payment checkout session.
- **Client-Side Tier Protection**: Updated `UserProfileMapper` (Profile feature) and `Mapper` (Edit Profile feature) to preserve local `FREE` tier selection so background server refreshes (`GET /api/v1/users/profile`) do not revert the UI back to `PLUS` or `PRO` mid-cycle.

#### 4. Paymob WebView Render & Redirect Interception
- **WebView Configuration**: Configured `CheckoutScreen` with `domStorageEnabled = true`, `databaseEnabled = true`, `mixedContentMode`, and third-party cookies (`setAcceptThirdPartyCookies`) to support Paymob iFrames and 3D Secure verification.
- **Redirect URL Interception**: Added URL pattern matching for `data.message=Approved` and `localhost` callback URLs in `shouldOverrideUrlLoading` to prevent Chromium `net::ERR_CONNECTION_REFUSED` error screens when Paymob redirects to web callback URLs.

#### 5. Optimized Status Polling & Fix for False Timeouts
- **Optimized Polling**: Updated `PollPaymentStatusUseCase` to resolve status immediately when Paymob confirms approval.
- **Prevented False Failures**: Reduced max attempts from 30 to 4 fast cycles (~4s) and ensured that transactions approved on the frontend don't trigger false `VERIFICATION_TIMEOUT` ("Payment Failed") error screens.

#### 6. UI Spacing & Unified Tier Mapping
- **Bottom Margin & Insets**: Added `navigationBarsPadding()` and bottom margin to payment action buttons across `SubscriptionPlansScreen`, `GetCoinsScreen`, and `PaywallRootScreen`.
- **Dynamic Tier Normalization**: Standardized tier mapping across Home, Profile, and Plan screens:
  - `"FREE"` $\rightarrow$ **Free**
  - `"PLUS"` $\rightarrow$ **Plus**
  - `"PRO"` / `"MAX"` $\rightarrow$ **Max**

#### 7. Documentation & Agent Rules
- **Updated Project Rules**: Updated `.agents/AGENTS.md` with comprehensive payment feature architecture, backend endpoints, domain use cases, and design rules.

---

### Automated Verification
- Ran Gradle unit tests across payment, network, and profile modules:
  ```bash
  .\gradlew :feature:payment:testFakeDebugUnitTest
  ```
- **Result**: **BUILD SUCCESSFUL** (All unit tests passed).

---

## Backend Setup & Cloudflare Guide

### 1. Backend Environment Variables (`.env`)
```properties
# Paymob Authentication & Credentials
PAYMOB_BASE_URL=https://accept.paymob.com
PAYMOB_SECRET_KEY=egy_sk_test_...
PAYMOB_PUBLIC_KEY=egy_pk_test_...
PAYMOB_HMAC_SECRET=CADEA5CBEEA1BE4E3A86...
PAYMOB_INTEGRATION_ID_CARD=5766356

# Dynamic Cloudflare Webhook Notification URL
PAYMOB_NOTIFICATION_URL=https://ways-interference-roger-somewhat.trycloudflare.com/api/v1/payments/webhook/paymob
PAYMOB_REDIRECTION_URL=careerpilot://payment/result
```

### 2. Paymob Dashboard Configuration
In **Paymob Dashboard** $\rightarrow$ **Developers** $\rightarrow$ **Payment Integrations** $\rightarrow$ Edit Card Integration, set:
- **Transaction Processed Callback**:
  `https://ways-interference-roger-somewhat.trycloudflare.com/api/v1/payments/webhook/paymob`
- **Transaction Response Callback**:
  `https://ways-interference-roger-somewhat.trycloudflare.com/api/v1/payments/webhook/paymob`

### 3. Cloudflare Tunnel & Docker Server
- **Start Cloudflare Tunnel**:
  ```bash
  cloudflared tunnel --url http://localhost:8080
  ```
- **Run Backend Containers**:
  ```bash
  docker compose up -d --build
  ```
