# V380 Animal Guard Pro 38 Test Report

Scope: professional menu UI, background-only mode, hidden backend defaults, no restricted Accessibility permission.

## Static checks

- PASS: no old Pro 23 package references
- PASS: Java package paths
- PASS: rough brace balance
- PASS: no Accessibility service in manifest

## Feature checks

- PASS: No Accessibility service in manifest
- PASS: Professional menu Home
- PASS: Professional menu Guard
- PASS: Professional menu Animals
- PASS: Professional menu Events
- PASS: Professional menu Diagnostics
- PASS: Professional menu Settings
- PASS: Hidden backend defaults
- PASS: No technical input fields in MainActivity
- PASS: No OfficialTapService in MainActivity

## Java smoke test

```text
PASS: background-only animal classifier smoke test
```

## Limitation

Because Accessibility is removed, Pro 24 cannot auto-press the V380 siren button. It runs background alerts and opens V380 manually.
