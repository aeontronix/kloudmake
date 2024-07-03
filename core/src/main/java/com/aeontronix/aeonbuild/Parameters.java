/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.dsl.InvalidScriptException;
import com.aeontronix.aeonbuild.dsl.Parameter;
import org.antlr.v4.runtime.Token;

import java.util.*;

public class Parameters {
    private final ArrayList<Parameter> parameters = new ArrayList<>();
    private final HashMap<String, Parameter> namedParameters = new HashMap<>();

    public synchronized List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public synchronized Map<String, Parameter> getNamedParameters() {
        return Collections.unmodifiableMap(namedParameters);
    }

    public synchronized void addParameter(Token location, Parameter parameter) throws InvalidScriptException {
        if (!namedParameters.isEmpty()) {
            throw new InvalidScriptException("Unnamed parameters must not be present after named ones",
                    "[" + location.getLine() + ":" + location.getCharPositionInLine(), null, null);
        }
        parameters.add(parameter);
    }

    public synchronized void addNamedParameter(String name, Parameter parameter) {
        namedParameters.put(name, parameter);
    }

    public int size() {
        int size = parameters.size();
        if (namedParameters != null) {
            size += namedParameters.size();
        }
        return size;
    }
}
