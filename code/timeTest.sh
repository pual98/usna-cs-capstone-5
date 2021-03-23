#!/bin/bash

if [ "$1" == "-gui" ]; then
    TORUN="IDS"
elif [ "$1" == "-test" ]; then
    TORUN="Client"
else
    echo -e "This program runs a distributed version of the IDS.\nPlease run as a gui or as a test of the distribution"
    echo "./timeTest.sh [-gui] OR [-test]"
    exit;
fi

make -q
if ! make -q; then
    echo -e "${red}You need to run 'make'${reset}"
    exit
fi
# Create tempdirs
tmp_dir1=$(mktemp -d -t ciXXXXXXXXXX)

cp ./*".class" "$tmp_dir1/"

HOST=$(hostname)

trap "kill %1; killall java" SIGINT SIGTERM
if [ "$HOST" == "csmidn" ];
then
    cp "file.txt" "filterCommands.txt" "threeClusters.csv" "large.csv" "$tmp_dir1/"
    cd "$tmp_dir1" || exit; java Server
elif [ "$HOST" == "lnx1065211govt" ];
then
    cp "file.txt" "filterCommands.txt" "Fast Snort Data/file1.txt" "threeClusters.csv" "large.csv" "$tmp_dir1/"
    # cd "$tmp_dir1" || exit; java IDS -f "$tmp_dir1/file1.txt"
    cd "$tmp_dir1" || exit; java $TORUN -testing -host -file "$tmp_dir1/file1.txt"
else
    cp "file.txt" "filterCommands.txt" "Fast Snort Data/file1.txt" "threeClusters.csv" "large.csv" "$tmp_dir1/"
    # cd "$tmp_dir1" || exit; java IDS -f "$tmp_dir1/file1.txt"
    cd "$tmp_dir1" || exit; java $TORUN -testing -file "$tmp_dir1/file1.txt"
fi

# ...
rm -rf "$tmp_dir1"
killall java
