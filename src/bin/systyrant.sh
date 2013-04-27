#!/bin/bash

SYSTYRANT_ANT=/usr/share/systyrant

JOPTS=""

if [ "$1" = "-jdebug" ];
then
    JOPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 ${JOPTS}"
fi

java -classpath "${SYSTYRANT_ANT}/lib/*" ${JOPTS} com.kloudtek.systyrant.cli.Cli $@
