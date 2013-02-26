#!/bin/bash

ant -Dversion=0.0~dev deb
sudo dpkg -i _build/artifacts/systyrant.deb