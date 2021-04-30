#!/bin/bash

rm -f *.dot *.ps

dir=`echo $1 | rev | cut -d "/" -f 2- | rev`
class=`echo $1 | tr "/" "."`

javac "$dir"/*.java
rrrun -tool=VD -field=FINE -array=FINE -noTidGC -availableProcessors=4 $class

for dotfile in `ls *.dot`
do
    psfile=`echo $dotfile | rev | cut -d "." -f 2- | rev`
    dot -Tps $dotfile -o "$psfile".ps 
done

mkdir -p output/$class
rm -f output/$class/*

mv *.dot *.ps output/$class

rm "$dir"/*.class
