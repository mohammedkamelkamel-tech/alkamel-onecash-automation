# الكامل — أتمتة ون كاش

Android app to monitor incoming One Cash SMS receipts, map payer names to recipient phone numbers, manage prepaid-card inventory, and optionally send a matching card by SMS.

## Safety defaults
- Automatic sending is **OFF** by default.
- Only SMS from the configured wallet sender is processed.
- Payer names must be an exact normalized match to a saved customer.
- Each incoming message is deduplicated by a stable fingerprint.
- A card is reserved before sending and returned to stock if SMS send fails.
- Unknown names, unsupported amounts, empty stock, and ambiguous cases are logged for manual review.

## Build
Open this project in Android Studio, or use GitHub Actions (Actions tab → Android build → Run workflow). APK is uploaded as an artifact.

## Setup
1. Install the APK and grant SMS permissions.
2. In Settings, enter the exact SMS sender ID/address for One Cash.
3. Import card TXT files by denomination.
4. Add customer payer names and recipient phone numbers.
5. Test with automatic sending disabled; inspect the transfer log.
6. Enable auto-send only after confirming sender ID, message parsing, stock and customer mapping.

## Message format supported
```
استلمت 100.00
من ماجد علي صالح ا
رصيدك791.64 ر.ي
```
The parser supports Arabic/English digits and decimal amounts. It is intentionally conservative: messages not matching the incoming-payment format are ignored.

## Privacy
Data is stored locally on the device. No server or cloud sync is included. Backup/export should be done only to a secure location.
