# Pro 29 Test Plan

## 1. Manual siren test

Open app -> Home -> TEST SIREN NOW.

Report:
- Siren: Yes/No
- Last siren status
- Protocol result

## 2. Cloud test

Open app -> Guard -> CLOUD TEST ONCE.

Report:
- Cloud status
- Last event ID
- Snapshot available: Yes/No

## 3. AI profile test

Open AI tab:
- Balanced
- Sensitive
- Strict

Check that profile changes are reflected in AI card.

## 4. Real event test

Start Guard and wait for a real V380 event.
Report:
- Last animal
- Confidence
- Was siren triggered
- Last analysis
