javac test/$1.java;
rrrun -tool=VD -field=FINE -array=FINE -noTidGC -availableProcessors=4 test.$1;
dot -Tps TXgraph.dot -o outfile.ps;