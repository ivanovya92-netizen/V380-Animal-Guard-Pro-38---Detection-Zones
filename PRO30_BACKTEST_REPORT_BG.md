# Pro30 Backtesting Report

## Protected siren model

The working S01 direct siren model was preserved:
- DirectSirenProtocol still contains the S01 direct replay flow.
- Accessibility is not used.
- V380 UI tap is not used.
- S01 constants are validated as:
  - H2D/H79/H2F/7F = 256 bytes
  - H0121/8D/B900/BC = 16 bytes

## Backtest coverage

Built-in cases:
1. Boar at night -> siren
2. Unknown ground animal at night -> siren
3. Person -> no siren
4. Fox -> no siren
5. Dog -> no siren
6. No object -> no siren
7. Unknown day -> no siren
8. Bird -> no siren
9. Cat -> no siren
10. Low-light boar/pig -> siren

## UI

The UI is simplified and organized into:
- Home
- Guard
- AI
- Events
- Backtest
- Settings
