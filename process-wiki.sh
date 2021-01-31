#!/usr/bin/env zsh

HERE=`pwd`
SOURCE=./resources/org
DEST=./resources/org-new

rm -rf $DEST
cp -r $SOURCE $DEST
lein run -m org-wiki-tools.core process-wiki
cd $DEST
git add -A
echo 'autocommit: ' `date +"%dth %B, %Y"`
git commit -am "autocommit: `date +"%dth %B, %Y"`"
cd $HERE
./archive.sh
