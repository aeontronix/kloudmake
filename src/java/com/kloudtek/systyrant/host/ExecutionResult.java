/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

public class ExecutionResult {
    private int retCode;
    private String output;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
