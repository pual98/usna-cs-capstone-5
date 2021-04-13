#!/bin/bash

# Colors
red=$(tput setaf 1)
green=$(tput setaf 2)
yellow=$(tput setaf 3)
reset=$(tput sgr0)

# The following is the execute sequence which will be executed on each of the
# ssh connections, saved to the "EXECUTE" variable
if [ "$1" == "-gui" ];
then
read -r -d '' EXECUTE <<- "SSH"
    killall java;
    sleep 1;
    WORK_DIRECTORY="$PWD/Documents/usna-cs-capstone-5/code/";
    cd "$WORK_DIRECTORY";
    ./timeTest.sh -gui;
SSH
elif [ "$1" == "-test" ]
then
    echo "test"
read -r -d '' EXECUTE <<- "SSH"
    killall java;
    sleep 1;
    WORK_DIRECTORY="$PWD/Documents/usna-cs-capstone-5/code/";
    cd "$WORK_DIRECTORY";
    ./timeTest.sh -test;
SSH
else
    echo -e "This program runs a distributed version of the IDS.\nPlease run as a gui or as a test of the distribution"
    echo "./timeTest.sh [-gui] OR [-test]"
    exit;
fi

echo -e "${green}Welcome to the Intrusion Detection System testing"
echo -e "Authors: Laylon Mokry, Patrick Bishop, Paul Slife, Jose Quiroz"
echo -e "\n${yellow}WARNING: Before running this script complete the following:"
echo -e "setup ssh-keys by running the command: ssh-copy-id m2xxxxx@csmidn.academy.usn.edu"
echo -e "You may have already completed this. If a key is generated, don't set a password (just click enter)"
echo -e "Then, ssh into csmidn.academy.usna.edu and make sure the usna-cs-capstone-5\ngithub code is downloaded into your ~/Documents/ directory and checkout the\n'testBranch' branch"

# Exit if the user is not ready
echo -e -n "\nReady to begin [y/n] ${reset}"
read -r CONTINUE
if [ "$CONTINUE" != "y" ];
then
    exit
fi

echo -e "This may take a moment...${reset}"
echo -e -n "\n"

echo -e -n "Enter username w/ m [m21xxxx]: "
read -r USERNAME

# Array of computers to connect to. One server, three clients
declare -a computerList=(
"$USERNAME@csmidn.academy.usna.edu"
"$USERNAME@lnx1065211govt.academy.usna.edu"
"$USERNAME@lnx1065863govt.academy.usna.edu"
"$USERNAME@lnx1065864govt.academy.usna.edu"
)

#"$USERNAME@lnx1065866govt.academy.usna.edu"
#"$USERNAME@lnx1065868govt.academy.usna.edu"
#"$USERNAME@lnx1065870govt.academy.usna.edu"

# If this program received SIGINT, SIGTERM, EXIT, then run the kill_sequence
# function
trap "killall ssh; kill_sequence" SIGINT SIGTERM EXIT

function kill_sequence()
{
    # Kill sequence makes sure the java programs are all closed on each of the
    # computers
    for i in "${!computerList[@]}"
    do
        ssh -f -X "${computerList[$i]}"  "killall java;" > /dev/null 2>&1;
    done
  exit
}

# Get len of computerClient list
len=${#computerList[@]}
for i in "${!computerList[@]}"
do
    if [ "$i" -eq 0 ]
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
