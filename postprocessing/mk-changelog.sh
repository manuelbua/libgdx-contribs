#!/bin/sh

git log --pretty  --summary HEAD master . | git2cl | sed 's/\*\s\:\s//' | sed 's/\<\(.*\)\(@\)\(.*\)/\1[at]\3/'
