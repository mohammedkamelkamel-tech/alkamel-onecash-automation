# Test checklist before enabling auto-send

- Verify One Cash sender address exactly in Settings.
- Confirm incoming SMS sample parses amount and payer name.
- Add test customer mapping and verify exact name matching.
- Import test TXT inventory, verify counts and duplicates.
- Keep auto-send OFF during initial tests.
- Test on a spare/test recipient SIM before real transactions.
- Confirm card is marked SENT only after Android SMS_SENT result.
- Check unknown payer, unsupported denomination, duplicate SMS, and empty inventory are REVIEW/ignored.
- Confirm SMS permissions granted and device SIM can send SMS.
