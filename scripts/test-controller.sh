#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/controller-tests
javac -encoding UTF-8 -d build/controller-tests \
  app/src/main/java/studio/foldglass/GlassMath.java \
  app/src/main/java/studio/foldglass/OpeningMotion.java \
  tests/GlassMathTest.java tests/OpeningMotionTest.java
java -cp build/controller-tests studio.foldglass.GlassMathTest
java -cp build/controller-tests studio.foldglass.OpeningMotionTest
