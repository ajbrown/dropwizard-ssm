#!/usr/bin/env bash
openssl aes-256-cbc -K $encrypted_682bf77c88dd_key -iv $encrypted_682bf77c88dd_iv -in build-support/codesigning.asc.enc -out build-support/codesigning.asc -d
gpg --fast-import build-support/codesigning.asc
mvn deploy -P sign,build-extras --settings build-support/mvnsettings.xml
