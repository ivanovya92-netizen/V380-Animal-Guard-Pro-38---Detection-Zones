# Pro31 BoarVision Report

## Protected siren model

The S01 direct siren model is preserved exactly from Pro30:
- H2D/H79/H2F/7F = 256 bytes
- H0121/8D/B900/BC = 16 bytes
- No Accessibility
- No UI tap

## Boar recognition work

Added BoarVisionEngine with:
- explicit boar/pig/hog/suinae labels
- boar anatomy features
- ground-zone and lower-body features
- hard negative classes
- review-required state for borderline detections
- real snapshot label collection

## Field learning

User can now mark the last event as:
- Boar / Wild Boar
- Not Boar
- Person
- Unknown

The labels are stored in a local learning dataset and can be copied for future model training.
