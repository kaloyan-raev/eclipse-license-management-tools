# README #

### Converting key file to byte array ###

Use the hexdump command to dump the file in decimal bytes:


```
#!bash

hexdump mydsa.pub -v -e '1/1 "%d" ", "'
```
