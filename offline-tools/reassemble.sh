#!/usr/bin/env bash
# Reassembles every split binary under offline-tools/ (files committed as
# <name>.part-aa, <name>.part-ab, ... to stay under GitHub's 100MB per-file
# limit without Git LFS) back into <name>, verifying each one against its
# committed <name>.sha256 checksum.
#
# Safe to run more than once: an already-reassembled file whose checksum
# still matches is left untouched.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

shopt -s nullglob
first_parts=("$SCRIPT_DIR"/*/*.part-aa "$SCRIPT_DIR"/*.part-aa)
shopt -u nullglob

if [ ${#first_parts[@]} -eq 0 ]; then
	echo "No split files found under $SCRIPT_DIR."
	exit 0
fi

for first_part in "${first_parts[@]}"; do
	dir="$(dirname "$first_part")"
	base="$(basename "$first_part" .part-aa)"
	target="$dir/$base"
	checksum_file="$dir/$base.sha256"

	if [ ! -f "$checksum_file" ]; then
		echo "ERROR: missing checksum file $checksum_file for $base" >&2
		exit 1
	fi
	expected_sha256="$(cat "$checksum_file")"

	if [ -f "$target" ]; then
		actual_sha256="$(shasum -a 256 "$target" | awk '{print $1}')"
		if [ "$expected_sha256" == "$actual_sha256" ]; then
			echo "$base already reassembled and verified, skipping."
			continue
		fi
		echo "$base exists but checksum does not match, rebuilding."
	fi

	echo "Reassembling $base from $(ls "$dir/$base".part-* | wc -l | tr -d ' ') part(s)..."
	tmp_target="$target.reassembling"
	cat "$dir/$base".part-* > "$tmp_target"

	actual_sha256="$(shasum -a 256 "$tmp_target" | awk '{print $1}')"
	if [ "$expected_sha256" != "$actual_sha256" ]; then
		echo "ERROR: checksum mismatch for $base" >&2
		echo "  expected: $expected_sha256" >&2
		echo "  actual:   $actual_sha256" >&2
		rm -f "$tmp_target"
		exit 1
	fi

	mv "$tmp_target" "$target"
	echo "$base reassembled and verified."
done
