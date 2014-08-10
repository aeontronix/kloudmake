#!/bin/bash

if [ "$1" = "-u" ];
then
    echo "Un-installing Kloudmake"
    rm -rf /usr/share/kloudmake
    rm -f /usr/bin/kloudmake
else
    echo "Installing Kloudmake"
    COUNT=`awk '/^__SOURCE__/ { print NR + 1; exit 0; }' $0`
    THIS=`pwd`/$0
    rm -rf /usr/share/kloudmake/*
    tail -n+${COUNT} ${THIS} | tar -C /usr/share -xj

    chmod +x /usr/share/kloudmake/bin/kloudmake.sh
    if [ ! -e '/usr/bin/kloudmake' ]; then
        echo "Creating symlink /usr/bin/kloudmake"
        ln -s /usr/share/kloudmake/bin/kloudmake.sh /usr/bin/kloudmake
    fi
    mkdir -p /usr/share/kloudmake/libs/
    mkdir -p /var/lib/kloudmake/libs/
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
