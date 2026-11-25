# Changelog\n\n## 0.7.0 (development)\n- Added report section with daily transfer totals, transfer status breakdown, and sent cards by denomination.\n- Parser now rejects fractional amounts instead of rounding.\n- Added parser unit tests.\n- Duplicate SMS guard for similar transfers within 90 seconds.\n- Card reservation checks update result to reduce concurrent double-reservation risk.\n\n## 0.6.0\n- Orange accent palette and updated high-contrast UI.\n- Added reports screen for daily transfer counts, status counts, and sent-card totals by denomination.\n- Added parser unit tests for Arabic/English digits, name normalization, missing fields, and fractional amounts.\n- Reject fractional transfer amounts to avoid rounding into a wrong card denomination.\n- Suppress near-identical duplicate SMS deliveries within a 90-second safety window.\n- Make card reservation conditional to avoid concurrent reservation races.\n- Auto-send remains OFF by default.\n\n# Changelog

## 0.4.0 — Project completion branch
- Native Android app with Arabic UI and local SQLite storage.
- One Cash SMS sender whitelist.
- Parse incoming amount and payer name from supported SMS template.
- Customer name-to-phone mapping with edit/delete actions.
- TXT card import by denomination and duplicate-code protection.
- Transfer deduplication and review/waiting states.
- Manual card sending for matched transfers.
- Optional auto-send, disabled by default.
- Card reservation and SMS send-result handling; failed sends restore stock.
- Database backup/restore via Android document picker.
- GitHub Actions debug APK build workflow.


## 0.5.0 — UI redesign
- New Arabic RTL-oriented dashboard and branded header.
- Bottom navigation for home, customers, stock, transfers, and settings.
- Searchable customer and pending-transfer lists.
- Redesigned stock cards and quick actions.
- Branded app theme, status/navigation bars, and launcher icon.
- Improved backup/restore confirmation and copy-stream handling.
