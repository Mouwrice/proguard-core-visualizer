echo "Running pre-commit hook..."

if [[ -z "$(git diff --name-only --staged)" ]]; then
  echo "No files staged. Commit not created."
  exit 1
fi

staged_files=$(git diff --name-only --staged "*.kt")

if [[ -z "$staged_files" ]]; then
  echo "No staged files to format."
  exit 0
fi

# Format only the staged files
./gradlew spotlessApply $staged_files

# Add the formatted files to the staging area
git add "$staged_files"

exit 0
