#!/bin/bash

red=$(tput setaf 1)
green=$(tput setaf 2)
reset=$(tput sgr0)

WORK_DIRECTORY="/home/mids/m216240/Documents/usna-cs-capstone-5/code"

cd "$WORK_DIRECTORY" || exit

make -q
if ! make -q; then
    echo -e "${red}You need to run 'make'${reset}"
    exit
fi
# Create tempdirs
tmp_dir1=$(mktemp -d -t ciXXXXXXXXXX)

cp ./*".class" "$tmp_dir1/"
HOST=$(hostname)

if [ "$HOST" == "csmidn" ];
then
    cp "file.txt" "filterCommands.txt" "threeClusters.csv" "large.csv" "$tmp_dir1/"
    trap "kill %1" SIGINT
    cd "$tmp_dir1" || exit; java Server &
else
    cp "file.txt" "filterCommands.txt" "Fast Snort Data/file1.txt" "threeClusters.csv" "large.csv" "$tmp_dir1/"
    trap "kill %1" SIGINT
    cd "$tmp_dir1" || exit; java IDS -f "$tmp_dir1/file1.txt" &
fi

# ...
rm -rf "$tmp_dir1"
#filename.sh
