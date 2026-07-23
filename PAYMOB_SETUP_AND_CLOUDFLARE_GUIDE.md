# Paymob & Cloudflare Setup Guide for Career Pilot Backend

This guide outlines the backend environment variables required for Paymob payment gateway integration and the Cloudflare Tunnel setup for processing webhooks.

---

## 1. Backend Environment Variables (`.env` / `application.yml`)

The backend requires the following Paymob credentials configured in `application.yml` or environment variables:

```properties
# Paymob Authentication & Credentials
PAYMOB_API_KEY=your_paymob_api_key_here
PAYMOB_INTEGRATION_ID=your_card_integration_id_here
PAYMOB_FRAME_ID=your_paymob_iframe_id_here
PAYMOB_HMAC_SECRET=your_paymob_hmac_secret_here

# Public Host / Cloudflare Base URL (for Paymob Redirection & Callbacks)
PAYMENT_CALLBACK_URL=https://<your-subdomain>.trycloudflare.com/payment/result
```

### Spring Boot Config (`application.yml`)
```yaml
paymob:
  api-key: ${PAYMOB_API_KEY}
  integration-id: ${PAYMOB_INTEGRATION_ID}
  iframe-id: ${PAYMOB_FRAME_ID}
  hmac-secret: ${PAYMOB_HMAC_SECRET}
```

---

## 2. Paymob Dashboard Configuration

In your [Paymob Dashboard](https://accept.paymob.com/):

1. **Developers $\rightarrow$ Payment Integrations**:
   - Select your **Card Integration** (Online Card).
   - Copy the **Integration ID** to `PAYMOB_INTEGRATION_ID`.
2. **Developers $\rightarrow$ IFrames**:
   - Create or select an iFrame (e.g. Unified Checkout / Standard Card Form).
   - Copy the **Frame ID** to `PAYMOB_FRAME_ID`.
3. **Settings $\rightarrow$ Account Info**:
   - Copy the **HMAC Secret** to `PAYMOB_HMAC_SECRET` (used by backend to verify webhook signatures).

---

## 3. Cloudflare Tunnel Setup for Webhooks & Local Testing

Paymob sends **server-to-server POST webhooks** when transactions succeed or fail. Since Paymob cannot reach `localhost:8080`, a public HTTPS tunnel is required.

### Step 1: Install Cloudflare CLI (`cloudflared`)
- **Windows (winget)**:
  ```powershell
  winget install --id Cloudflare.cloudflared
  ```
- **macOS (homebrew)**:
  ```bash
  brew install cloudflared
  ```

### Step 2: Start Cloudflare Tunnel
Run the following command while the Spring Boot backend is running on port `8080`:

```bash
cloudflared tunnel --url http://localhost:8080
```

Cloudflare will generate a public HTTPS URL, for example:
`https://random-subdomain.trycloudflare.com`

### Step 3: Configure Webhook Callbacks in Paymob Dashboard
Go to **Developers $\rightarrow$ Payment Integrations $\rightarrow$ Edit Integration**:

- **Transaction Processed Callback**:
  `https://random-subdomain.trycloudflare.com/api/v1/payments/paymob-callback`
- **Transaction Response Callback**:
  `https://random-subdomain.trycloudflare.com/api/v1/payments/paymob-callback`

---

## 4. Summary of Endpoints

| Route | Method | Description |
| :--- | :--- | :--- |
| `POST /api/v1/wallet/top-up` | `POST` | Initiates coin purchase checkout session |
| `POST /api/v1/subscriptions/upgrade` | `POST` | Initiates subscription upgrade checkout session (`PLUS`, `PRO`) |
| `POST /api/v1/subscriptions/downgrade` | `POST` | Schedules non-payment downgrade (`FREE`) |
| `POST /api/v1/subscriptions/cancel` | `POST` | Cancels subscription renewals |
| `POST /api/v1/payments/paymob-callback` | `POST` | Paymob HMAC Webhook receiver endpoint |
