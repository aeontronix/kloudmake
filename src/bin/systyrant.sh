#!/bin/bash

SYSTYRANT_ANT=/usr/share/systyrant

java -classpath "$SYSTYRANT_ANT/lib/*" com.kloudtek.systyrant.cli.Cli $@
