#!/usr/bin/env bash

./gradlew --quiet :installDist
./build/install/handsaw/bin/handsaw "$@"