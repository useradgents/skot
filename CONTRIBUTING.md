# Create a new version
- update version name in `buildSrc/src/main/kotlin/Versions`
- Add new version to changelog in `docs/changelog`
- launch `./script/check.sh` to verify that all tests still pass.
- push change
- add tag with the version as name 
- create a release for this tag on github
- go to [jitpack](https://jitpack.io/#useradgents/skot), start the compilation, and wait for it to finish to verify that the build is successful.
