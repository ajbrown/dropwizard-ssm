#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" == 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_682bf77c88dd_key -iv $encrypted_682bf77c88dd_iv -in build-support/codesigning.asc.enc -out build-support/codesigning.asc -d
    gpg --fast-import build-support/codesigning.asc
fi
