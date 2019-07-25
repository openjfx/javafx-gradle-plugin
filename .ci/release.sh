#!/usr/bin/env bash

./gradlew publishPlugins -Pgradle.publish.key=$PUBLISH_KEY -Pgradle.publish.secret=$PUBLISH_SECRET

# Update version by 1
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Replace first occurrence of
# version 'TRAVIS_TAG' 
# with 
# version 'newVersion-SNAPSHOT'
sed -i "0,/^version '$TRAVIS_TAG'/s//version '$newVersion-SNAPSHOT'/" build.gradle

git commit build.gradle -m "Upgrade version to $newVersion-SNAPSHOT" --author "Github Bot <githubbot@gluonhq.com>"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/openjfx/javafx-gradle-plugin HEAD:master