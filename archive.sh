#!/usr/bin/env bash

HERE=`pwd`
cd ~/Documents/archivebox
while IFS= read -r line; do
  echo $line | archivebox add
done < $HERE/to_archive.txt
cd $HERE
