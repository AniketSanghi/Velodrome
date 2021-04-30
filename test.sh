#!/bin/bash

rm -f *.dot *.ps

javac test/$1.java
rrrun -tool=VD -field=FINE -array=FINE -noTidGC -availableProcessors=4 test.$1

for dotfile in *.dot
do
    dot -Tps $dotfile -o $dotfile.ps 
done
