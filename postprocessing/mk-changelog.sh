#!/bin/sh

if [ -z "$2" ]; then
	# use provided exp
	#git log postprocessing "$1" --date=short --format="%ad%x20%an <%ae> %n%n%x09* %s%n" | sed 's/@/[at]/'
	git log --pretty --numstat --summary "$1" . | git2cl | sed 's/\<\(.*\)\(@\)\(.*\)/\1[at]\3/'
else
	# compose a very basic exp, "between version tags"
	#git log postprocessing "$1".."$2" --date=short --format="%ad%x20%an <%ae> %n%n%x09* %s%n" | sed 's/@/[at]/'
	git log --pretty --numstat --summary "$1".."$2" . | git2cl | sed 's/\<\(.*\)\(@\)\(.*\)/\1[at]\3/'
fi