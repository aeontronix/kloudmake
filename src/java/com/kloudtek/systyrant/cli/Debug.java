/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.cli;

import org.apache.commons.io.IOUtils;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 18/09/12
 * Time: 21:50
 * To change this template use DataFile | Settings | DataFile Templates.
 */
public class Debug {


    public static void main(String[] args) throws Exception {
//        Process exec = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "apt-get -o Dpkg::Options::='--force-confdef' -o Dpkg::Options::='--force-confold' -f -q -y install --no-install-recommends aespipe=2.4c-1"});
//        IOUtils.copy(exec.getInputStream(), System.out);
//        IOUtils.copy(exec.getErrorStream(), System.out);
        System.out.println("===================================================");
        Process exec2 = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "apt-get -o Dpkg::Options::='--force-confdef' -o Dpkg::Options::='--force-confold' -f -q -y install --no-install-recommends aespipe=2.4c-1"},
                new String[]{"DEBIAN_FRONTEND=noninteractive"}, new File("."));
        IOUtils.copy(exec2.getInputStream(), System.out);
        IOUtils.copy(exec2.getErrorStream(), System.out);
    }
}
