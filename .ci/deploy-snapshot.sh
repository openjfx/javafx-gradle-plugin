#!/usr/bin/env bash

# Find project version
ver=$(./gradlew properties -q | grep "version:" | awk '{print $2}')

# deploy if snapshot found
if [[ $ver == *"SNAPSHOT"* ]] 
then
    ./gradlew publish -PsonatypeUsername=$SONATYPE_USERNAME -PsonatypePassword=$SONATYPE_PASSWORD
fi