#!/usr/bin/env bash

./gradlew --quiet :installDist
./build/install/mixin-i18n/bin/mixin-i18n "$@"