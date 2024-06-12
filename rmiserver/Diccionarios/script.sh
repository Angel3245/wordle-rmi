#!/bin/bash

for i in $( ls *.dic )
do
	cat $i | sed 's/ *$//g' | grep -v [0-9] | iconv -f utf8 -t ascii//TRANSLIT | awk -F '/' 'length($1) == 5 {print $1}' | tr [:lower:] [:upper:] > $i.midic
done
