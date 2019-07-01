#!/usr/bin/env bash

openssl aes-256-cbc -K $encrypted_3f6a2c09ff46_key -iv $encrypted_3f6a2c09ff46_iv -in .ci/sonatype.gpg.enc -out sonatype.gpg -d
./gradlew publish --info -PsonatypeUsername=$SONATYPE_USERNAME -PsonatypePassword=$SONATYPE_PASSWORD -Psigning.keyId=$GPG_KEY_ID -Psigning.password=$GPG_KEY_PASSPHRASE -Psigning.secretKeyRingFile=sonatype.gpg