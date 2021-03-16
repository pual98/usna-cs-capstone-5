#!/bin/bash

# Colors
red=$(tput setaf 1)
green=$(tput setaf 2)
reset=$(tput sgr0)

echo -e "${green}Welcome to the Intrusion Detection System testing"
echo -e "Authors: Laylon Mokry, Patrick Bishop, Paul Slife, Jose Quiroz"
echo -e "This may take a moment...${reset}"
echo -e -n "\n"

numProcesses=$(ps aux | grep -v grep | grep -i -e VSZ -e java | wc -l)
if [ "$numProcesses" != "1" ]; then
    killall java;
    sleep 1;
fi

make -q
if ! make -q; then
    echo -e "${red}You need to run 'make'${reset}"
    exit
fi
# Create tempdirs
tmp_dir1=$(mktemp -d -t ciXXXXXXXXXX)
tmp_dir2=$(mktemp -d -t ciXXXXXXXXXX)
tmp_dir3=$(mktemp -d -t ciXXXXXXXXXX)
tmp_dir4=$(mktemp -d -t ciXXXXXXXXXX)

cp ./*".class" "$tmp_dir1/"
cp ./*".class" "$tmp_dir2/"
cp ./*".class" "$tmp_dir3/"
cp ./*".class" "$tmp_dir4/"

cp "file.txt" "filterCommands.txt" "threeClusters.csv" "large.csv" "$tmp_dir1/"
cp "file.txt" "filterCommands.txt" "Fast Snort Data/file1.txt" "threeClusters.csv" "large.csv" "$tmp_dir2/"
cp "file.txt" "filterCommands.txt" "Fast Snort Data/file2.txt" "threeClusters.csv" "large.csv" "$tmp_dir3/"
cp "file.txt" "filterCommands.txt" "Fast Snort Data/file3.txt" "threeClusters.csv" "large.csv" "$tmp_dir4/"


trap "kill %1; kill %2; kill %3; kill %4" SIGINT

cd "$tmp_dir1" || exit; java Server &
sleep 5;
cd "$tmp_dir2" || exit; java IDS -f "$tmp_dir2/file1.txt" &
cd "$tmp_dir3" || exit; java IDS -f "$tmp_dir3/file2.txt" &
cd "$tmp_dir4" || exit; java IDS -f "$tmp_dir4/file3.txt"

# ...
rm -rf "$tmp_dir1"
rm -rf "$tmp_dir2"
rm -rf "$tmp_dir3"
rm -rf "$tmp_dir4"
