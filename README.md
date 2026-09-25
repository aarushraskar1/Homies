# Homies Gym

Advanced Android fitness MVP built with Kotlin + Jetpack Compose.

## Included
- Dashboard, training library, progress, profile
- Local workout history/stats via DataStore
- Homies Pro subscription architecture using Google Play Billing 9.1.0
- Premium workout gating
- Subscription paywall
- Material 3 dark UI
- Clean separation between repository, ViewModel, UI, and billing

## Play Console setup
Create a subscription product with ID `homies_premium_monthly` and configure its base plan/offer. Real purchase verification should be performed by a secure backend using Google Play Developer APIs before production launch.

## Build
```bash
gradle assembleDebug
gradle test
```
