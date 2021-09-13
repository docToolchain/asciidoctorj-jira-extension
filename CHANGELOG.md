# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### :rocket: Features & Enhancements
- Implement Jira inline macro

### :broom: Housekeeping
- Setup validate-gradle-wrapper pipeline via github actions
- Setup run tests pipeline for java 8,9,11 and distribution zulu and adopt via github actions
- Use git lfs to keep repository size small
- Introduce a changelog file based on [Keep a changelog](https://keepachangelog.com/en/1.0.0/)
- Publish test results to PullRequests
- Replace JDK9 with JDK16 in test matrix
- Publish Snapshot to Maven central [(#4)](https://github.com/docToolchain/asciidoctorj-jira-extension/issues/4)