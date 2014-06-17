#!/bin/bash

git commit -am "commit msg: $1"
git push origin master

ssh -p 9001 ags670@localhost "(cd ~/thesis/ ; git pull)"
