/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.sal.binding.generator.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * The default implementation of the SourceCodeGenerator interface that generates readable source code
 * for a runtime generated class. The appendField/appendMethod methods output source code to a temporary
 * StringBuilder. When outputGeneratedSource is called, the entire class source code is generated and
 * written to a file under a specified directory.
 *
 * @author Thomas Pantelis
 */
public class DefaultSourceCodeGenerator implements SourceCodeGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSourceCodeGenerator.class);

    private static final String GENERATED_SOURCE_DIR_PROP = "org.opendaylight.yangtools.sal.generatedCodecSourceDir";

    private final StringBuilder builder = new StringBuilder();
    private final String generatedSourceDir;

    /**
     * Constructor.
     *
     * @param generatedSourceDir the directory in which to put generated source files. If null, the directory
     *     is obtained from a system property (<i>org.opendaylight.yangtools.sal.generatedCodecSourceDir</i>) or
     *     defaults to "generated-codecs".
     */
    public DefaultSourceCodeGenerator( String generatedSourceDir ) {
        if( generatedSourceDir != null ) {
            this.generatedSourceDir = generatedSourceDir;
        }
        else {
            this.generatedSourceDir = System.getProperty( GENERATED_SOURCE_DIR_PROP, "generated-codecs" );
        }
    }

    @Override
    public void appendField(CtField field, String value) {
        try {
            builder.append('\n')
                    .append(Modifier.toString(field.getModifiers()))
                    .append(' ').append(field.getType().getName()).append(' ')
                    .append(field.getName());
            if (value != null) {
                builder.append(" = ").append(value);
            }

            builder.append(";\n");
        } catch (NotFoundException e) {
            LOG.error("Error building field source for " + field.getName(), e);
        }
    }

    @Override
    public void appendMethod(CtMethod method, String code) {
        try {
            builder.append('\n')
                    .append(Modifier.toString(method.getModifiers()))
                    .append(' ').append(method.getReturnType().getName())
                    .append(' ').append(method.getName()).append("( ");

            CtClass[] paramTypes = method.getParameterTypes();
            if (paramTypes != null) {
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    builder.append(paramTypes[i].getName()).append(" $")
                            .append(i + 1);
                }
            }

            builder.append(" )\n").append(code).append("\n\n");
        } catch (NotFoundException e) {
            LOG.error("Error building method source for " + method.getName(), e);
        }
    }

    @Override
    public void outputGeneratedSource(CtClass ctClass) {
        String name = ctClass.getName();

        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append(Modifier.toString(ctClass.getModifiers()))
                .append(" class ").append(ctClass.getSimpleName());

        try {
            CtClass superClass = ctClass.getSuperclass();
            if (superClass != null) {
                classBuilder.append(" extends ").append(superClass.getName());
            }

            CtClass[] interfaces = ctClass.getInterfaces();
            if (interfaces.length > 0) {
                classBuilder.append(" implements ");
                for (int i = 0; i < interfaces.length; i++) {
                    if (i > 0) {
                        classBuilder.append(", ");
                    }

                    classBuilder.append(interfaces[i].getName());
                }
            }

            classBuilder.append(" {\n").append(builder.toString())
                    .append("\n}");
        } catch (NotFoundException e) {
            LOG.error("Error building class source for " + name, e);
            return;
        }

        File dir = new File(generatedSourceDir);
        dir.mkdir();
        try (FileWriter writer = new FileWriter(new File(dir, name + ".java"))) {
            writer.append(classBuilder.toString());
            writer.flush();
        } catch (IOException e) {
            LOG.error("Error writing class source for " + name, e);
        }
    }
}
