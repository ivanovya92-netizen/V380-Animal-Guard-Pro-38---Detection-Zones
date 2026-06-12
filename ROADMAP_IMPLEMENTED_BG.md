# V380 Animal Guard Pro 38 Roadmap Update

1. Added UBox-style animal recognition alerts.
2. Added categories for boar/pig, fox, dog, cat, bird, person, and unknown.
3. Added separate notification list for selected animals.
4. Added separate siren list for selected animals.
5. Added editable keyword profile per animal.
6. Added last animal and confidence into Guard card.
7. Added animal result into event history and diagnostics.
8. Added loud local animal notification toggle.
9. Added ntfy animal notification toggle.
10. Added option to write or skip animal result in event history.
11. Added option to allow siren only for selected animals.
12. Added SAFE_NIGHT as recommended field mode.
13. Added SMART_ANIMAL as experimental smart mode.
14. Added unknown movement at night fallback.
15. Preserved official V380 Pro More -> Alarm tapper.
16. Preserved direct relay lab as fallback only.
17. Preserved Android and OxygenOS foreground-service fixes.
18. Preserved GitHub Node 24 workflow setting.
19. Preserved built-in Devin defaults including token and tap coordinates.
20. Preserved diagnostics, runtime reset, event history and logs.

## Limitation

Boar recognition is not yet a custom trained detector. It is keyword and ML-label based until real boar snapshots are collected.


## Previous roadmap

# V380 Animal Guard Pro 38 Roadmap Update

1. UBox-style animal alert center added.
2. Separate animal categories: boar/pig, fox, dog, cat, bird, person, unknown.
3. Editable keyword profile for each animal category.
4. Separate notify animal list and siren animal list.
5. Animal result is written into event history and diagnostics.
6. Animal local loud notification toggle.
7. Animal ntfy forwarding toggle.
8. Smart fallback for unknown night movement.
9. Option to block siren until selected animal is confirmed.
10. SAFE_NIGHT remains practical field mode until real boar samples prove classifier.
11. UI sections reorganized: Quick setup, Boar Guard policy, Animal alerts, Animal profiles, Official tapper.
12. Dashboard now shows last animal and confidence.
13. Built-in Devin defaults include animal profiles.
14. Official V380 More to Alarm tapper remains primary siren method.
15. Direct relay lab remains fallback only.
16. GitHub Node 24 env preserved.
17. Android/OxygenOS foreground service fix preserved.

## Previous roadmap

# V380 Animal Guard Pro 38 Roadmap and Implemented Changes

This file documents the product-hardening changes requested for a serious field-use version.

1. **Product mode** — Rename and restructure into V380 Animal Guard Pro, not a research prototype. Status: **Implemented**.
2. **Guard state** — Add clear READY, EVENT_FOUND, ANALYZING, SIREN_REQUESTED, SIREN_SENT, COOLDOWN states. Status: **Implemented**.
3. **Armed hours** — Allow siren only in night armed window by default 20:00-07:00. Status: **Implemented**.
4. **Trigger modes** — SAFE_NIGHT, ANY_MOTION, SMART_ANIMAL, TEST_NEXT_EVENT. Status: **Implemented**.
5. **Official V380 tapper** — Use real V380 Pro More then Alarm path instead of relying only on relay guesses. Status: **Implemented**.
6. **Tap coordinates** — Editable More and Alarm X/Y percent coordinates. Status: **Implemented**.
7. **Tap delays** — Editable delay after opening V380 and delay between More and Alarm. Status: **Implemented**.
8. **Accessibility status** — Show whether tapper service is enabled in Health/Guard. Status: **Implemented**.
9. **Manual tap tests** — More only, Alarm only, More then Alarm, Open V380 then Alarm. Status: **Implemented**.
10. **Manual service siren test** — Service-driven siren request to simulate automated event path. Status: **Implemented**.
11. **Cooldown** — Prevent siren loops with configurable cooldown. Status: **Implemented**.
12. **Hourly limit** — Prevent excessive siren activations with max sirens per hour. Status: **Implemented**.
13. **Double alarm option** — Optional double tap on Alarm button. Status: **Implemented**.
14. **Test next event only** — Allow one-event test then revert to SAFE_NIGHT. Status: **Implemented**.
15. **Smart snapshot analysis** — Keep ML Kit snapshot analysis and write results into history. Status: **Implemented**.
16. **Smart animal keywords** — Editable animal keyword set for SMART_ANIMAL mode. Status: **Implemented**.
17. **Unknown night fallback** — Option to trigger unknown movement at night when ML is inconclusive. Status: **Implemented**.
18. **Notification stack** — Keep urgent local notification and ntfy forwarding. Status: **Implemented**.
19. **Quiet hours** — Keep quiet local sound while preserving ntfy/status. Status: **Implemented**.
20. **Baseline protection** — Do not siren on old events unless explicitly enabled. Status: **Implemented**.
21. **Event dedup** — Upsert event rows instead of duplicate analysis rows. Status: **Implemented**.
22. **Diagnostics** — Expand diagnostics with guard, siren, tap, cooldown, and event state. Status: **Implemented**.
23. **Runtime reset** — Reset runtime state without deleting settings. Status: **Implemented**.
24. **History/log clear** — Clear history and logs from UI. Status: **Implemented**.
25. **Battery shortcut** — Open battery optimization screen. Status: **Implemented**.
26. **Notification shortcut** — Open notification settings. Status: **Implemented**.
27. **Boot restart** — Preserve restart-after-boot setting. Status: **Implemented**.
28. **Foreground-service Android 14 fix** — FOREGROUND_SERVICE_DATA_SYNC and dataSync foreground type. Status: **Implemented**.
29. **OxygenOS installation stability** — Unique package, stable release keystore, signing v1/v2/v3. Status: **Implemented**.
30. **GitHub Actions future warning** — FORCE_JAVASCRIPT_ACTIONS_TO_NODE24 in workflow. Status: **Implemented**.
31. **Private defaults** — Built-in Devin defaults including token. Status: **Implemented**.
32. **Direct relay fallback** — Keep selected direct relay probe as fallback diagnostics. Status: **Implemented**.
33. **One tap startup** — Save, cloud test and start guardian in one button. Status: **Implemented**.
34. **Supervised test** — 30-minute supervised aggressive mode. Status: **Implemented**.
35. **Cloud error handling** — Notify on token, network, and cloud errors with backoff. Status: **Implemented**.
36. **Guard dashboard** — Dedicated dashboard, guard, and health cards. Status: **Implemented**.
37. **Ntfy topic edit** — Configurable ntfy topic. Status: **Implemented**.
38. **Snapshot open** — Open last snapshot directly. Status: **Implemented**.
39. **Manual analysis** — Analyze last snapshot manually. Status: **Implemented**.
40. **Roadmap in repo** — Include this roadmap in the repository package. Status: **Implemented**.

## Current limitation

Pig-specific recognition is not yet proven on real boar snapshots. SMART_ANIMAL mode uses available ML labels and keyword heuristics. SAFE_NIGHT mode remains the practical field mode until real boar image samples are collected.
