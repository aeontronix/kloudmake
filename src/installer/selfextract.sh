#!/bin/bash

if [ "$1" = "-u" ];
then
    echo "Un-installing SysTyrant"
    rm -rf /usr/share/systyrant
    rm -f /usr/bin/systyrant
else
    echo "Installing SysTyrant"
    COUNT=`awk '/^__SOURCE__/ { print NR + 1; exit 0; }' $0`
    THIS=`pwd`/$0
    rm -rf /usr/share/systyrant/*
    tail -n+${COUNT} ${THIS} | tar -C /usr/share -xj

    chmod +x /usr/share/systyrant/bin/systyrant.sh
    if [ ! -e '/usr/bin/systyrant' ]; then
        echo "Creating symlink /usr/bin/systyrant"
        ln -s /usr/share/systyrant/bin/systyrant.sh /usr/bin/systyrant
    fi
    mkdir -p /usr/share/systyrant/libs/
    mkdir -p /var/lib/systyrant/libs/
fi



#if [ -d '/usr/share/ant/lib/' ]; then
#    if [ ! -e '/usr/share/ant/lib/ant-contrib.jar' ]; then
#        echo "Creating symlink /usr/share/ant/lib/ant-contrib.jar"
#        ln -s /usr/share/buildmagic/ant-contrib.jar /usr/share/ant/lib/ant-contrib.jar
#    fi
#    if [ ! -e '/usr/share/ant/lib/buildmagic.jar' ]; then
#        echo "Creating symlink /usr/share/ant/lib/buildmagic.jar"
#        ln -s /usr/share/buildmagic/buildmagic.jar /usr/share/ant/lib/buildmagic.jar
#    fi
#    if [ ! -e '/usr/share/ant/lib/ivy.jar' ]; then
#        echo "Creating symlink /usr/share/ant/lib/ivy.jar"
#        ln -s /usr/share/buildmagic/ivy-2.3.0.jar /usr/share/ant/lib/ivy.jar
#    fi
#fi

echo "Finished"
exit 0
__SOURCE__
