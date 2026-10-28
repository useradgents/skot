set -e
set -x
./gradlew generator:test
./gradlew plugin:test
bash scripts/migration/2.0.0-ua/test-migrate-flavors.sh
./gradlew core:test viewcontract:test modelcontract:test
./gradlew coreAndroid:testDebugUnitTest
./gradlew model:testDebugUnitTest
./gradlew viewmodel:testDebugUnitTest
./gradlew coreAndroid:assembleDebugAndroidTest model:assembleDebugAndroidTest
./gradlew viewlegacy:compileDebugAndroidTestSources
./gradlew publishToMavenLocal -x test -x check
(cd sample && ./gradlew skot:skGenerate)
(cd sample && ./gradlew assembleBlueDevDebug assembleBlueProdDebug assembleRedDevDebug assembleRedProdDebug testBlueDevDebugUnitTest testBlueProdDebugUnitTest testRedDevDebugUnitTest testRedProdDebugUnitTest)
