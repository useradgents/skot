set -e
set -x
./gradlew generator:test
./gradlew plugin:test
./gradlew core:test viewcontract:test modelcontract:test
./gradlew coreAndroid:testDebugUnitTest
./gradlew model:testDebugUnitTest
./gradlew viewmodel:testDebugUnitTest
./gradlew coreAndroid:assembleDebugAndroidTest model:assembleDebugAndroidTest
./gradlew viewlegacy:compileDebugAndroidTestSources
