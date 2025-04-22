#!/usr/bin/env bash

vers=(1.*)
for idx in "${!vers[@]}"; do
    if [ $idx -ne 0 ]; then
        current=${vers[$idx]}
        prev=${vers[$idx-1]}
        diff -ruN $current $prev > "run/${current}_to_${prev}.diff"
    fi
done
