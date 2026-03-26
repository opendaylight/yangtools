/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;

/**
 * Template for generating JAVA interfaces.
 */
class InterfaceTemplate extends AbstractInterfaceTemplate {
    @NonNullByDefault
    InterfaceTemplate(final GeneratedType type) {
        super(type);
    }

    @Override
    CharSequence body() {
        //        «type.formatDataForJavaDoc.wrapToDocumentation»
        //        «type.annotations.generateAnnotations»
        //        «generatedAnnotation»
        //        public interface «type.simpleName»
        //            «superInterfaces»
        //        {
        //
        //            «generateInnerClasses»
        //
        //            «generateInnerEnumTypeObjects(enums)»
        //
        //            «generateConstants»
        //
        //            «generateMethods»
        //
        //        }
        //

        final var sc = new StringConcatenation();
        sc.append(wrapToDocumentation(formatDataForJavaDoc(type())));
        sc.newLineIfNotEmpty();
        sc.append(generateAnnotations(type().getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append(generatedAnnotation());
        sc.newLineIfNotEmpty();
        sc.append("public interface ");
        sc.append(type().simpleName());
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append(superInterfaces(), "    ");
        sc.newLineIfNotEmpty();
        sc.append("{");
        sc.newLine();
        sc.newLine();
        sc.append("    ");
        sc.append(generateInnerClasses(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateInnerEnumTypeObjects(enums), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateConstants(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateMethods(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("}");
        sc.newLine();
        sc.newLine();
        return sc;
    }

    /**
     * {@return string with the code for the interface declaration in JAVA format}
     */
    private String superInterfaces() {
        final var ifaces = type().getImplements();
        if (ifaces.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder()
            .append("extends\n");
        final var it = ifaces.iterator();
        while (true) {
            sb.append(importedName(it.next()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(",\n");
        }
    }

    /**
     * {@return string with the source code for inner classes in JAVA format}
     */
    private CharSequence generateInnerClasses() {
        if (enclosedGeneratedTypes.isEmpty()) {
            return "";
        }
        final var innerClasses = enclosedGeneratedTypes.stream()
            .map(this::generateInnerClass)
            .filter(str -> !str.isEmpty())
            .toList();
        if (innerClasses.isEmpty()) {
            return "";
        }

        final var sc = new StringConcatenation();
        final var it = innerClasses.iterator();
        while (true) {
            sc.append(it.next());
            if (!it.hasNext()) {
                return sc;
            }
            sc.newLine();
        }
    }
}
