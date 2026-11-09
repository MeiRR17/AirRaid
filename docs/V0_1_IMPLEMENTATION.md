# AirRaid v0.1.0 implementation notes

This file records the implementation scope and remaining acceptance evidence for the Java 21/Maven build foundation. **This is not the v1.0.0 production release.**

## Implemented
- Adopted Maven's default `src/main/java`, `src/main/resources` and `src/test/java` layout.
- Added a minimal `pom.xml` targeting JDK 21 with pinned build/test plugins and a runnable Main-Class JAR manifest.
- Moved 23 binary media assets without changing their Git blob SHA.
- Replaced fragile filesystem-relative image/font lookups with classpath resource loading.
- Fixed the case-sensitive background filename, GIF placeholder and repeated font parsing during painting.
- Ensured Swing startup is dispatched through the EDT.
- Added JUnit tests for bundled resources and a standalone Xvfb GUI startup smoke test.
- Added a 5-minute-bounded Java build workflow on relevant PRs.

## Local verification
```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.1.0.jar
```

The second command requires a graphical desktop session and must be checked for a visible main window, a rules window, and an exit without crash. In CI the smoke helper is run under a virtual display:

```sh
xvfb-run -a timeout 20s java -cp target/classes:target/test-classes GuiSmoke
```

## Scope limits
The existing game still has immortal plane/bomb worker threads and mutates Swing components off the EDT; these defects are scheduled for **v0.2.0**. The inconsistent canyon-hit rule is scheduled for **v0.3.0**. A verified commercial distribution asset license audit and native installers are later milestones.

Do not tag `v0.1.0` or check off the milestone before the focused compile, classpath-resource tests, and GUI launch smoke have passed and the PR has been integrated into `master`.
