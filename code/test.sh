#!/bin/bash

# Colors
read -r -d '' EXECUTE <<- "SSH"

red=$(tput setaf 1)
green=$(tput setaf 2)
reset=$(tput sgr0)

WORK_DIRECTORY="/home/m216240/Documents/usna-cs-capstone-5/code/"

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
SSH


#### START SCRIPT
red=$(tput setaf 1)
green=$(tput setaf 2)
reset=$(tput sgr0)

echo -e "${green}Welcome to the Intrusion Detection System demo"
echo -e "Authors: Laylon Mokry, Patrick Bishop, Paul Slife, Jose Quiroz"
echo -e "This may take a moment...${reset}"
echo -e -n "\n"

declare -a computerList=(
"midn.cs.usna.edu"
"m216240@lnx1065211govt.academy.usna.edu"
"m216240@lnx1065863govt.academy.usna.edu"
"m216240@lnx1065864govt.academy.usna.edu")

trap "kill %1; kill %2; kill %3; kill%4" SIGINT
for computer in "${computerList[@]}"
do
    ssh -X "$computer" \'"$EXECUTE"\' &
done
