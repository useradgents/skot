## Add variant for project


## Add Variant (APP1, APP2 )

In the skot module, , add a skSwitchTask for each Env you use in your project :

* in the build.gradle.kts
  `skSwitchTask("TaskEnvVariantName", "dev", "APP1" )`

* launch gradle sync

* All the task added are visible in the gradle task group skot_switch_variant__

## Change current Variant

* launch the switch task you want to use

* launch gradle sync (or gradle clean) task

## Bash script to update Env on ci/cv client

```bash
#!/usr/bin/env bash
./gradlew skot:yourChangeEnvTask
./gradlew clean
```
