# V380 Animal Guard Pro 38 Backtest Report

Scope: static code checks, pure Java classifier tests, package integrity checks, and release packaging checks.

- PASS: No old package/name references
- PASS: Java package declarations match paths
- PASS: Rough brace balance
- PASS: AnimalClassifier backtest

```text
boar OK -> boar 98%, matched specific keyword 'boar' notify=true siren=true
boar OK -> boar 98%, matched specific keyword 'pig' notify=true siren=true
fox OK -> fox 92%, matched specific keyword 'fox' notify=true siren=false
dog OK -> dog 88%, matched specific keyword 'dog' notify=true siren=false
cat OK -> cat 88%, matched specific keyword 'cat' notify=true siren=false
bird OK -> bird 84%, matched specific keyword 'bird' notify=true siren=false
person OK -> person 95%, matched specific keyword 'person' notify=true siren=false
unknown OK -> unknown 60%, matched unknown/generic keyword 'no objects' notify=true siren=true
none OK -> none 0%, No animal keyword found in ML summary, below threshold 55% notify=false siren=false
```


## Important limitation

A real Android APK build cannot be executed inside this container because the Android SDK and Gradle runner are not installed here. The GitHub Actions workflow is still required to prove the Android build. The workflow remains configured with compileSdk 35, targetSdk 35, JDK 17, Gradle 8.12 and Node 24 opt-in.

## Fix made after backtest

The previous animal classifier allowed generic labels such as `animal` and `mammal` to override specific results. Example: a fox sample could be classified as dog because dog had a higher generic score. Pro 21 fixes this by separating specific keywords from generic unknown-animal keywords.
