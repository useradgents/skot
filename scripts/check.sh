set -e
set -x
./gradlew generator:jvmTest
./gradlew viewmodel:jvmTest
./gradlew model:jvmTest
./gradlew viewlegacy:compileDebugAndroidTestSources
