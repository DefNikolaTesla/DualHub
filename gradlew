#!/usr/bin/env sh
# Standard Gradle wrapper launcher script.
# If gradle/wrapper/gradle-wrapper.jar is missing, run `gradle wrapper` once
# (with a system-installed Gradle) to regenerate it before using ./gradlew.

DIR="$(cd "$(dirname "$0")" && pwd)"
exec gradle -p "$DIR" "$@"
