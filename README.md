# Homies Gym

Complete Android fitness app foundation plus a production-oriented Google Play subscription backend.

## Features
- Dashboard, daily mission, training programs and progress
- Local workout history/stats
- Homies Pro premium gating
- Monthly and yearly Google Play subscriptions
- Purchase restore and acknowledgement
- Google Play Developer API subscription verification
- Google Play Real-time Developer Notifications (RTDN) endpoint
- PostgreSQL entitlement schema and indexes
- Secure environment configuration
- Backend health endpoint

## Product IDs
- homies_premium_monthly
- homies_premium_yearly

Create these subscription products/base plans in Google Play Console before testing purchases.

## Production security
Google service-account credentials stay on the backend. Replace the development x-homies-user-id header with real authenticated Homies JWT/session middleware before launch.

## Launch Commands

Android:
gradle assembleDebug

Backend:
cd backend
npm install
npm run build
npm start

## Verification Guide
1. Build the Android app and verify dashboard/training flows.
2. Configure Play Console license testers and subscription products, then verify purchase and restore.
3. Configure Pub/Sub RTDN and verify /health plus subscription lifecycle processing.