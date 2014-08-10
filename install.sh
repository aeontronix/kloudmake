#!/bin/bash

ant -Dversion=0.0~dev dist
sudo sh _build/artifacts/kloudmake-installer.sh
