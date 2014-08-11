/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.util;

import com.kloudtek.kloudmake.annotation.Default;
import com.kloudtek.kloudmake.annotation.Function;
import com.kloudtek.kloudmake.annotation.Param;
import com.sun.javadoc.*;

import java.io.*;
import java.lang.annotation.Annotation;

public class GenerateManualAppendix {
    public static void main(String[] args) {
        String[] rargs = new String[]{"-d", "_build/", "-sourcepath", "src/java", "-subpackages", "com.kloudtek.kloudmake"};
        PrintWriter err = new PrintWriter(System.err);
        com.sun.tools.javadoc.Main.execute("STDocGen", err, err, err, GenerateManualAppendix.class.getName(), rargs);
    }

    public static boolean start(RootDoc root) throws IOException {
        String dest = null;
        for (String[] options : root.options()) {
            if (options[0].equals("-d")) {
                dest = options[1];
            }
        }
        if (dest == null) {
            root.printNotice("destdir (-d) missing");
            return false;
        }
        try (BufferedWriter w = new BufferedWriter(new FileWriter(dest + File.separator + "builtin-functions.xml"))) {
            w.write("<appendix xmlns=\"http://docbook.org/ns/docbook\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://docbook.org/ns/docbook http://docbook.org/xml/5.0/xsd/docbook.xsd\">\n");
            w.write("\t<title>Built-in functions</title>\n");
            for (ClassDoc classDoc : root.classes()) {
                for (MethodDoc methodDoc : classDoc.methods()) {
                    for (AnnotationDesc annotationDesc : methodDoc.annotations()) {
                        if (annotationDesc.annotationType().qualifiedName().equals(Function.class.getName())) {
                            String functionName = annoValue(annotationDesc, "value");
                            if (functionName == null) {
                                functionName = methodDoc.name();
                            }
                            w.write("\t<section>\n");
                            w.write("\t\t<title>");
                            w.write(functionName);
                            w.write("</title>\n");
                            w.write("\t\t<para>\n");
                            w.write(methodDoc.commentText().replace("<p>", "<para>").replace("</p>", "</para>").replace("<br/>", "\n"));
                            w.write("\n\t\t</para>\n");
                            if (methodDoc.parameters().length > 0) {
                                w.write("<para>Parameters:</para>\n");
                                w.write("<itemizedlist>");
                                for (Parameter parameter : methodDoc.parameters()) {
                                    w.write("<listitem><para>\n<emphasis role='strong'>");
                                    w.write(parameter.name());
                                    AnnotationDesc paramAnno = anno(parameter.annotations(), Param.class);
                                    if (paramAnno != null) {
                                        String def = annoValue(paramAnno, "def");
                                        if (def == null) {
                                            AnnotationDesc defAnno = anno(parameter.annotations(), Default.class);
                                            if (defAnno != null) {
                                                def = annoValue(defAnno, "value");
                                            }
                                        }
                                        if (def != null) {
                                            w.write(" ( default: ");
                                            w.write(def);
                                            w.write(" )");
                                        }
                                    }
                                    w.write("</emphasis> : ");
                                    w.write(paramDocs(methodDoc, parameter.name()));
                                    w.write("\n</para></listitem>\n");
                                }
                                w.write("</itemizedlist>\n");
                            }
                            w.write("\t</section>\n");
                        }
                    }
                }
            }
            w.write("</appendix>");
        }
        return true;
    }

    private static AnnotationDesc anno(AnnotationDesc[] annotations, Class<? extends Annotation> paramClass) {
        for (AnnotationDesc annotationDesc : annotations) {
            if (annotationDesc.annotationType().qualifiedName().equals(paramClass.getName())) {
                return annotationDesc;
            }
        }
        return null;
    }

    private static String paramDocs(MethodDoc methodDoc, String name) {
        for (ParamTag paramTag : methodDoc.paramTags()) {
            if (paramTag.parameterName().equals(name)) {
                return paramTag.parameterComment();
            }
        }
        return "";
    }

    private static String annoValue(AnnotationDesc annotationDesc, String name) {
        for (AnnotationDesc.ElementValuePair pair : annotationDesc.elementValues()) {
            if (pair.element().name().equals(name)) {

                return pair.value().value().toString();
            }
        }
        return null;
    }

    public static int optionLength(String option) {
        switch (option) {
            case "-d":
                return 2;
            default:
                return 0;
        }
    }
}
