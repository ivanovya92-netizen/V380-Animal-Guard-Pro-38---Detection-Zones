# V380 Animal Guard Pro 38 Functional and Code Test Report

Scope: Pro 23 Field Console changes, safety limits, history filters, tap calibration UI, package integrity and animal classification.

## Static code checks

- Old package references: PASS
- Java package declarations: PASS
- Rough brace balance: PASS

## Feature presence checks

- PASS: Field Console section
- PASS: Setup checklist renderer
- PASS: Emergency stop
- PASS: Safe disarm
- PASS: Tap calibration nudges
- PASS: History filters
- PASS: Private diagnostics
- PASS: Siren master field
- PASS: Night limit field
- PASS: Siren master enforced
- PASS: Night limit enforced

## Animal classifier Java tests

```text
PASS: boar notifies
PASS: boar sirens
PASS: fox stays fox
PASS: fox notify only default
PASS: unknown night category
PASS: unknown night siren default
PASS: unknown day no notify
PASS: unknown day no siren
PASS: fox siren when selected
PASS: config has siren master field
PASS: config has night limit field
TOTAL_PASSED=11
```

## Guard policy simulation

```text
PASS: SAFE_NIGHT triggers inside armed hours | (True, 'SAFE_NIGHT', SimState(lastSirenAt=1000, hourCount=1, nightCount=1, triggerModeStored='SAFE_NIGHT'))
PASS: Outside armed hours blocks | OUTSIDE_ARMED_HOURS
PASS: Siren master lock blocks | (False, 'SIREN_MASTER_OFF', SimState(lastSirenAt=0, hourCount=0, nightCount=0, triggerModeStored='SAFE_NIGHT'))
PASS: Night limit blocks | NIGHT_LIMIT
PASS: Hourly limit blocks | HOURLY_LIMIT
PASS: Cooldown blocks second siren | (True, 'SAFE_NIGHT', False, 'COOLDOWN')
PASS: Test next event triggers and returns SAFE_NIGHT | (True, 'TEST_NEXT_EVENT', SimState(lastSirenAt=1000, hourCount=1, nightCount=1, triggerModeStored='SAFE_NIGHT'))
PASS: Selected animal only waits for SMART_ANIMAL | WAIT_SMART_ANIMAL
PASS: Tapper disabled blocks before counters | (False, 'TAPPER_DISABLED', SimState(lastSirenAt=0, hourCount=0, nightCount=0, triggerModeStored='SAFE_NIGHT'))
PASS: Auto siren off blocks | AUTO_OFF
```

## Android build limitation

This environment does not include the Android SDK, so final Android compilation must still run in GitHub Actions. The workflow is included and configured for SDK 35, JDK 17, Gradle 8.12 and Node 24 opt-in.
