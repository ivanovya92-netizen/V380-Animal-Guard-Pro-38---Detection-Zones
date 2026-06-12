# Pro32 CameraBoarVision Report

## Supplied camera evidence

The user supplied real V380 night IR images from the exact garden. The visible boar pattern is:
- lower field / ground-zone movement
- grey/dark compact body
- side profile: long low body
- rear profile: compact vertical body
- movement across lower-left to lower-middle area
- strong right-side bright vegetation should be ignored

## Implemented

- Exact camera ROI: lower-left/lower-middle field area
- Right bright bush ignored by ROI cutoff
- Adaptive dark blob detection
- Connected-component silhouette scoring
- Side profile and rear profile support
- Camera-boar-score added into BoarVisionEngine
- Built-in backtest extended with supplied-image-style cases

## Protected siren model

The working S01 model remains unchanged.
