#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
instance_dir="${1:-/Volumes/SSD/PrismLauncher/instances/idk meowtils test}"
if [[ $# -gt 1 || "${1:-}" == --help || "${1:-}" == -h ]]; then
    printf 'Usage: %s [Prism instance directory]\n' "$0"
    exit 0
fi
mods_dir="$instance_dir/minecraft/mods"
if [[ ! -d "$mods_dir" ]]; then
    printf 'Prism mods directory does not exist: %s\n' "$mods_dir" >&2
    exit 1
fi
if [[ -z "${JAVA_HOME:-}" || ! -x "$JAVA_HOME/bin/javac" ]] || [[ "$("$JAVA_HOME/bin/javac" -version 2>&1)" != javac\ 25* ]]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home
fi
if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
    printf 'JDK 25 not found at %s; set JAVA_HOME to your JDK 25 installation.\n' "$JAVA_HOME" >&2
    exit 1
fi
cd -- "$project_dir"
./gradlew test build

mod_version="$(awk -F= '$1 == "mod_version" {sub(/\r$/, "", $2); print $2}' gradle.properties)"
archive_name="$(awk -F= '$1 == "archives_base_name" {sub(/\r$/, "", $2); print $2}' gradle.properties)"
if [[ ! "$mod_version" =~ ^[A-Za-z0-9._+-]+$ || ! "$archive_name" =~ ^[A-Za-z0-9._-]+$ ]]; then
    printf 'Invalid artifact name or version in gradle.properties\n' >&2
    exit 1
fi
artifact="$project_dir/build/libs/$archive_name-$mod_version.jar"
[[ -s "$artifact" ]] || { printf 'Built jar not found: %s\n' "$artifact" >&2; exit 1; }

# Stage the new jar before moving any installed version out of the way.
staged_jar="$(mktemp "$mods_dir/.meowtils-install.XXXXXX")"
trap 'if [[ -n "$staged_jar" && -f "$staged_jar" ]]; then rm -f -- "$staged_jar"; fi' EXIT
cp -- "$artifact" "$staged_jar"
cmp -- "$artifact" "$staged_jar"

backup_root="$instance_dir/minecraft/meowtils/build-backups"
mkdir -p -- "$backup_root"
backup_dir="$(mktemp -d "$backup_root/install.XXXXXX")"
shopt -s nullglob
for installed in "$mods_dir"/meowtils-*.jar; do
    mv -- "$installed" "$backup_dir/"
done
destination="$mods_dir/$(basename -- "$artifact")"
if ! mv -- "$staged_jar" "$destination"; then
    for previous in "$backup_dir"/*.jar; do mv -- "$previous" "$mods_dir/"; done
    printf 'Installation failed; previous jars restored.\n' >&2
    exit 1
fi
staged_jar=""
cmp -- "$artifact" "$destination"
printf '\nInstalled: %s\nPrevious jars: %s\nRestart the Prism instance to load the new build.\n' "$destination" "$backup_dir"
