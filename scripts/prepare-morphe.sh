#!/usr/bin/env bash
# Prepare the reviewed upstream source plus this repository's GPL-3.0 modifications.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
revision=8759b348497f39f799baf1bc34015f4b51605b80
sha256=56480d22d1cfd624e59d7aa825fa3c6464fb2ae4484959f95e53244a08e6cb2f
destination=${1:-"$root/build/metra-source"}
if [[ -e "$destination" ]]; then
  echo "Destination already exists; refusing to overwrite: $destination" >&2
  exit 1
fi
mkdir -p "$(dirname "$destination")"
parent=$(cd "$(dirname "$destination")" && pwd)
destination="$parent/$(basename "$destination")"
work=$(mktemp -d "$parent/.metra-prepare.XXXXXXXX")
trap 'rm -rf -- "$work"' EXIT
archive="$work/source.tar.gz"
if [[ -n "${METRA_ARCHIVE:-}" ]]; then
  cp -- "$METRA_ARCHIVE" "$archive"
else
  curl --fail --location --silent --show-error --retry 2 \
    "https://codeload.github.com/icysymmetra/tiktok-patches-for-morphe/tar.gz/$revision" -o "$archive"
fi
printf '%s  %s\n' "$sha256" "$archive" | sha256sum --check --status || {
  echo 'Upstream archive hash does not match the pinned revision.' >&2; exit 1;
}
# Reject links and unsafe names before extracting even a correctly hashed archive.
python3 - "$archive" <<'PY'
import pathlib, sys, tarfile
with tarfile.open(sys.argv[1], 'r:gz') as tar:
    members = tar.getmembers()
    roots = set()
    for m in members:
        p = pathlib.PurePosixPath(m.name)
        if p.is_absolute() or '..' in p.parts or not p.parts or not (m.isfile() or m.isdir()):
            raise SystemExit('Unsafe archive member: ' + m.name)
        roots.add(p.parts[0])
    if len(roots) != 1:
        raise SystemExit('Unexpected archive root')
PY
mkdir "$work/source"
tar --extract --gzip --file "$archive" --strip-components=1 --directory "$work/source" --no-same-owner
for change in "$root"/morphe/patches/*.patch; do
  patch --batch --forward --directory "$work/source" -p1 < "$change"
done
cp -R "$root/morphe/overlay/." "$work/source/"
mv -- "$work/source" "$destination"
echo "Prepared TikTok Mod patch source at: $destination"
echo "Base: icysymmetra/tiktok-patches-for-morphe@$revision (GPL-3.0)"
