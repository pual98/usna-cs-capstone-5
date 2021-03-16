#!/bin/bash

# Colors

red=$(tput setaf 1)
green=$(tput setaf 2)
reset=$(tput sgr0)

WORK_DIRECTORY="/home/mids/m216240/Documents/usna-cs-capstone-5/code/"
read -r -d '' EXECUTE <<- SSH
    cd "$WORK_DIRECTORY" || exit
    ./timeTest.sh
SSH

echo -e "${green}Welcome to the Intrusion Detection System demo"
echo -e "Authors: Laylon Mokry, Patrick Bishop, Paul Slife, Jose Quiroz"
echo -e "This may take a moment...${reset}"
echo -e -n "\n"

declare -a computerList=(
"midn.cs.usna.edu"
"m216240@lnx1065211govt.academy.usna.edu"
"m216240@lnx1065863govt.academy.usna.edu"
"m216240@lnx1065864govt.academy.usna.edu"
)

trap "killall ssh" SIGINT SIGTERM EXIT

for computer in "${computerList[@]}"
do
    if [ "$computer" == "midn.cs.usna.edu" ]
    then
        ssh -X "$computer" \'"$EXECUTE"\' &
        sleep 5;
    else
        ssh -X "$computer" \'"$EXECUTE"\' &
    fi
done
