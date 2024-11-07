unzip -j -aa -L MIRACL-master.zip
chmod 777 linux64
./pk-demo
find . -type f -not -name '*.a' -not -name '*.h' -not -name '*.o' -not -name '.git*'| xargs rm
