#!/bin/bash

# Colors
#BLACK='\033[0;30m'
#GREEN='\033[0;32m'
#ORANGE='\033[0;33m'
BLUE='\033[0;34m'
#PURPLE='\033[0;35m'
#CYAN='\033[0;36m'
#LIGHTGRAY='\033[0;37m'
#DARKGRAY='\033[1;30m'
#LIGHTRED='\033[1;31m'
LIGHTGREEN='\033[1;32m'
#YELLOW='\033[1;33m'
#LIGHTBLUE='\033[1;34m'
#LIGHTPURPLE='\033[1;35m'
#LIGHTCYAN='\033[1;36m'
#WHITE='\033[1;37m'

RED='\033[0;31m'
NC='\033[0m' #no color

echo -e "${LIGHTGREEN}Welcome to the Intrusion Detection System demo"
echo -e "Authors: Laylon Mokry, Patrick Bishop, Paul Slife, Jose Quiroz"
echo -e "This may take a moment...${NC}"
echo -e -n "\n"

numProcesses=$(ps aux | grep -v grep | grep -i -e VSZ -e java | wc -l)
if [ "$numProcesses" != "1" ]; then
    killall java;
    sleep 1;
fi

make -q
if ! make -q; then
    echo -e "${RED}You need to run 'make'${NC}"
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

cp "file.txt" "threeClusters.csv" "large.csv" "$tmp_dir1/"
cp "file.txt" "threeClusters.csv" "large.csv" "$tmp_dir2/"
cp "file.txt" "threeClusters.csv" "large.csv" "$tmp_dir3/"
cp "file.txt" "threeClusters.csv" "large.csv" "$tmp_dir4/"

trap 'kill %1; kill %2; kill %3; kill %4' SIGINT

cd "$tmp_dir1" || exit; java Server &
sleep 5;
cd "$tmp_dir2" || exit; java IDS &
cd "$tmp_dir3" || exit; java IDS &
cd "$tmp_dir4" || exit; java IDS

# ...
rm -rf "$tmp_dir1"
rm -rf "$tmp_dir2"
rm -rf "$tmp_dir3"
rm -rf "$tmp_dir4"
