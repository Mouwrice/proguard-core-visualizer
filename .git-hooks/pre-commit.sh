#!/usr/bin/bash

echo "Running pre-commit hook..."

staged_files=$(git diff --name-only --staged "*.kt")


# Format only the staged files
./gradlew spotlessApply

# Add the formatted files to the staging area if any
if [ -n "$staged_files" ]; then
    git add "$staged_files"
fi

exit 0
