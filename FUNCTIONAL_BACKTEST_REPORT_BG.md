# V380 Animal Guard Pro 38 Functional Backtest Report

Scope: functional decision backtesting for animal classification, trigger modes, siren policy, cooldown, hourly limit, selected-animal mode, and tapper-disabled safety.

## Static integrity

- Old package/name references: PASS
- Java package path declarations: PASS
- Rough brace balance: PASS

## AnimalClassifier functional backtest

```text
PASS: SMART_ANIMAL boar notifies
PASS: SMART_ANIMAL boar siren
PASS: boar category
PASS: pig maps to boar
PASS: fox category not overridden by generic mammal
PASS: fox notifies
PASS: fox does not siren by default
PASS: dog category
PASS: dog no siren by default
PASS: cat category
PASS: bird category
PASS: person category
PASS: person no siren by default
PASS: unknown night category
PASS: unknown night notifies
PASS: unknown night sirens by default
PASS: unknown day below threshold no notify
PASS: unknown day below threshold no siren
PASS: fox siren when explicitly selected
PASS: animal alerts off disables notify
PASS: siren still allowed if autoSiren and selected
PASS: auto siren off blocks siren
TOTAL_PASSED=22
```

## Guard policy functional simulation

```text
PASS: SAFE_NIGHT inside armed triggers siren | SAFE_NIGHT
PASS: SAFE_NIGHT outside armed blocks siren | OUTSIDE_ARMED_HOURS
PASS: ANY_MOTION inside armed triggers | ANY_MOTION
PASS: TEST_NEXT_EVENT triggers once | (True, 'TEST_NEXT_EVENT', SimState(lastSirenAt=1000, hourCount=1, triggerModeStored='SAFE_NIGHT', testNextStored=False))
PASS: Cooldown blocks second event | (True, 'SAFE_NIGHT', False, 'COOLDOWN')
PASS: Hourly limit blocks seventh siren | HOURLY_LIMIT
PASS: Tapper disabled blocks before quota | (False, 'TAPPER_DISABLED', SimState(lastSirenAt=0, hourCount=0, triggerModeStored='SAFE_NIGHT', testNextStored=False))
PASS: Selected-animal-only blocks non-smart blanket siren | WAIT_SMART_ANIMAL
```

## Functional issues found and fixed

1. **Unknown daytime spam risk**: Pro 21 allowed low-confidence unknown detections to notify even below threshold. Pro 22 applies the confidence threshold to unknown as well, unless it is armed-night unknown fallback with sufficient confidence.
2. **Accessibility disabled consumed quota/cooldown**: Pro 21 checked siren cooldown/quota before verifying that the Accessibility tapper was enabled. Pro 22 checks tapper readiness first in the service code, so failed setup does not consume cooldown or hourly siren quota.

## Remaining real-device tests

- GitHub Actions Android compile.
- Install APK on OxygenOS.
- Enable Accessibility service.
- Manual More-only tap.
- Manual Alarm-only tap.
- Manual More then Alarm.
- One real V380 event with TEST_NEXT_EVENT.
- SAFE_NIGHT overnight supervised run.

## Android build limitation

The current execution container does not include the Android SDK/Gradle runner, so the final Android compilation must still be proven in GitHub Actions.
