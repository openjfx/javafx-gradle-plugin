#!/usr/bin/env bash

./gradlew publishPlugins -Pgradle.publish.key=$PUBLISH_KEY -Pgradle.publish.secret=$PUBLISH_SECRET

# Update version by 1
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Update README with the latest released version
sed -i "0,/id 'org.openjfx.javafxplugin' version '.*'/s//id 'org.openjfx.javafxplugin' version '$TRAVIS_TAG'/" README.md
sed -i "0,/id(\"org.openjfx.javafxplugin\") version \".*\"/s//id(\"org.openjfx.javafxplugin\") version \"$TRAVIS_TAG\"/" README.md
sed -i "0,/'org.openjfx:javafx-plugin:.*'/s//'org.openjfx:javafx-plugin:$TRAVIS_TAG'/" README.md
sed -i "0,/\"org.openjfx:javafx-plugin:.*\"/s//\"org.openjfx:javafx-plugin:$TRAVIS_TAG\"/" README.md
git commit README.md -m "Use latest release v$TRAVIS_TAG in README"

# Replace first occurrence of
# version 'TRAVIS_TAG' 
# with 
# version 'newVersion-SNAPSHOT'
sed -i "0,/^version '$TRAVIS_TAG'/s//version '$newVersion-SNAPSHOT'/" build.gradle

git commit build.gradle -m "Upgrade version to $newVersion-SNAPSHOT" --author "Github Bot <githubbot@gluonhq.com>"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/openjfx/javafx-gradle-plugin HEAD:master