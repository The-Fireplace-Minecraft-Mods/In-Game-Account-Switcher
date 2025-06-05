#!/usr/bin/env bash
# temporary script to generate differences between versions

versions=(1.*)
# https://stackoverflow.com/a/6723516
for index in "${!versions[@]}"; do
    if [ $index -ne 0 ]; then
        current=${versions[$index]}
        previous=${versions[$index-1]}
        # BSD-style: diff -ruN "$current" "$previous" > "diff_${current}to${previous}.diff"
        diff --recursive --unified --new-file "$current" "$previous" > "diff_${current}to${previous}.diff"
    fi;
done
