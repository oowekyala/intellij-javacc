#!/bin/bash

# Release script for the idea plugin
# Execute it from the root directory

set -e


git checkout main -q

CHANGELOG_LOCATION="changelog.html"

function incr_ver_num() {
    ret=$(echo "$1" | awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')
    echo "$ret"
}

release_version=$(./gradlew properties -q  --console=plain | grep "version:" | awk '{print $2}')

echo "Preparing release for version $release_version, from branch main..."

read -p "Enter the name of the release tag (default v$release_version): " tagname

if [[ -z "$tagname" ]]; then
    tagname="v$release_version"
fi

git tag -fa "$tagname" -F "$CHANGELOG_LOCATION"

release_changelog=$(mktemp)

cp "$CHANGELOG_LOCATION" "$release_changelog"

echo "Publishing plugin to repository..."

./gradlew publishPlugin


echo "Pushing objects..."

git push origin main
git push --tags

default_nr=$(incr_ver_num "$release_version")

read -p "What's the version number of the next release? (default $default_nr)" next_release

if [[ -z "$next_release" ]]; then
    next_release="$default_nr"
fi

replacement="s/version = \"$release_version\"/version = \"$next_release\"/"

sed -i -e "$replacement" build.gradle.kts
git add build.gradle.kts

echo "\nResetting the changelog..."

read -r -d '' DEFAULT_CHANGELOG <<'EOF'
<p>What's new:

<ul>
    <li>TODO</li>
</ul>

<p>What's changed:
<ul>
    <li>TODO</li>
</ul>

<p>What's fixed:
<ul>
    <li>TODO</li>
</ul>
EOF

echo "$DEFAULT_CHANGELOG" > "$CHANGELOG_LOCATION"

git add "$CHANGELOG_LOCATION"
git commit -m "Prepare next development version $next_release"

echo "\nSuccessfully released $release_version, edit the release notes on Github with the contents of $release_changelog"
