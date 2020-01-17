#!/bin/bash

# Release script for the idea plugin
# Execute it from the root directory

set -e


git checkout master -q

CHANGELOG_LOCATION="idea/changelog.html"

function incr_ver_num() {
    ret=$(echo "$1" | awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')
    echo "$ret"
}

published_version=$(./gradlew properties -q  --console=plain | grep "version:" | awk '{print $2}')

cur_version=$(incr_ver_num "$published_version")

branch="v$cur_version"

git show-branch "v$cur_version" >> /dev/null

if [[ $? -eq 0 ]]; then
    echo "Preparing release for version $cur_version, from branch $branch..."
else
    branch=""

    echo "Current branches"

    git show-branch --list

    while true; do
        read -p "What is the topic branch for the next version? " branch

        quiet=$(git show-branch "$branch")

        case $? in
            0 ) break;;
            * ) echo "Please enter a valid branch name";;
        esac
    done
fi


echo "Merging branch $branch into master..."

git merge "$branch" --no-ff
#git br -d "$branch"


read -p "Enter the name of the release tag (default v$cur_version): " tagname

if [[ -z "$tagname" ]]; then
    tagname="v$cur_version"
fi

git tag -a "$tagname" -F "$CHANGELOG_LOCATION"


echo "Publishing plugin to repository..."

./gradlew :idea:publishPlugin


echo "Pushing objects..."

git push origin master
git push --tags


default_nr=$(incr_ver_num "$cur_version")

read -p "What's the version number of the next release? (default $default_nr)" next_release

if [[ -z "$next_release" ]]; then
    next_release="$default_nr"
fi

git checkout -b "v$next_release"

replacement="s/version = \"$cur_version\"/version = \"$next_release\"/"

echo "$replacement"

sed -e "$replacement" build.gradle.kts

git add build.gradle.kts
git commit -m "Bump version to $next_release"

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
git commit -m "Reset changelog"

echo "\nSuccessfully released $cur_version, xoxo"
