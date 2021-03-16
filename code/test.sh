#!/bin/bash

# Colors

red=$(tput setaf 1)
green=$(tput setaf 2)
yellow=$(tput setaf 3)
reset=$(tput sgr0)

read -r -d '' EXECUTE <<- "SSH"
    killall java;
    sleep 1;
    WORK_DIRECTORY="$PWD/Documents/usna-cs-capstone-5/code/";
    cd "$WORK_DIRECTORY";
    ./timeTest.sh;
    killall java;
SSH

echo -e "${green}Welcome to the Intrusion Detection System testing"
echo -e "Authors: Laylon Mokry, Patrick Bishop, Paul Slife, Jose Quiroz"
echo -e "\n${yellow}WARNING: Before running this script complete the following:"
echo -e "setup ssh-keys by running the command: ssh-copy-id m2xxxxx@csmidn.academy.usn.edu"
echo -e "If a key is generated, don't set a password (just click enter)"
echo -e "Then, ssh into midn.cs.usna.edu and make sure the usna-cs-capstone-5\ngithub code is downloaded into your ~/Documents/ directory and checkout the\n'testBranch' branch"

echo -e -n "\nReady to begin [y/n] ${reset}"
read -r CONTINUE

if [ "$CONTINUE" == "n" ];
then
    exit
fi

echo -e "This may take a moment...${reset}"
echo -e -n "\n"

echo -e -n "Enter username w/ m [m21xxxx]: "
read -r USERNAME

declare -a computerList=(
"$USERNAME@csmidn.academy.usna.edu"
"$USERNAME@lnx1065211govt.academy.usna.edu"
"$USERNAME@lnx1065863govt.academy.usna.edu"
"$USERNAME@lnx1065864govt.academy.usna.edu"
)

trap "killall ssh" SIGINT SIGTERM EXIT

len=${#computerList[@]}
for i in "${!computerList[@]}"
do
    if [ "${computerList[$i]}" == "midn.cs.usna.edu" ]
    then
        echo "Starting server"
        printf -v _ %q "$EXECUTE"
        ssh -f -X "${computerList[$i]}"  "$_"
        sleep 5;
    elif [ $(("$len"-1)) -ne "$i" ]
    then
        echo "Starting normal"
        printf -v _ %q "$EXECUTE"
        ssh -f -X "${computerList[$i]}"  "$_"
    else
        echo "Starting last"
        printf -v _ %q "$EXECUTE"
        ssh -X "${computerList[$i]}"  "$_"
    fi
done
