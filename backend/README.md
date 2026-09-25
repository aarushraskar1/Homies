# Homies Billing API
Google Play subscription verification and RTDN processing.
Copy .env.example to .env. Never commit service-account keys.
Endpoints: GET /health, GET /v1/subscriptions/me, POST /v1/subscriptions/verify, POST /v1/rtdn/google-play.
Replace the development x-homies-user-id header with real Homies JWT authentication before production.