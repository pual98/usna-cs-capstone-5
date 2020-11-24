#!/bin/bash
echo "This may take a moment"
numProcesses=$(ps aux | grep -v grep | grep -i -e VSZ -e java | wc -l)
if [ $numProcesses != "1" ]; then
    killall java;
    sleep 1;
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

cd "$tmp_dir1"; java Server &
sleep 5;
cd "$tmp_dir2"; java IDS &
cd "$tmp_dir3"; java IDS &
cd "$tmp_dir4"; java IDS

# ...
rm -rf "$tmp_dir1"
rm -rf "$tmp_dir2"
rm -rf "$tmp_dir3"
rm -rf "$tmp_dir4"
