/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.Splitter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;

/**
 * Intermediate Java-based parts under {@link BaseTemplate}.
 */
abstract class AbstractBaseTemplate extends JavaFileTemplate {
    private static final Splitter NL_SPLITTER = Splitter.on('\n');

    AbstractBaseTemplate(final @NonNull GeneratedType type) {
        super(type);
    }

    AbstractBaseTemplate(final AbstractJavaGeneratedType javaType, final GeneratedType type) {
        super(javaType, type);
    }

    final @NonNull String generate() {
        final var sb = new StringBuilder()
            .append("package ").append(type().getPackageName()).append(";\n");

        // Has side-effects
        final var body = body();

        final var importBlock = generateImportBlock();
        if (!importBlock.isEmpty()) {
            sb.append(importBlock).append('\n');
        }
        return sb.append(body).toString();
    }

    /**
     * Generate the body of this Java file, i.e. the entire class declaration.
     *
     * @return Body of this Java file
     */
    abstract @NonNull CharSequence body();

    // Helper patterns
    static final @NonNull String fieldName(final GeneratedProperty property) {
        return "_" + property.getName();
    }

    /**
     * Template method which generates method parameters with their types from {@code parameters}.
     *
     * @param parameters list of parameter instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    final @NonNull String generateParameters(final @NonNull List<MethodSignature.Parameter> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedName(parameter.type())).append(' ').append(parameter.name());
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * Template method which generates sequence of the names of the class attributes from {@code parameters}.
     *
     * @param parameters group of generated property instances which are transformed to the sequence of parameter names
     * @return string with the list of the parameter names of the {@code parameters}
     */
    static final @NonNull String asArguments(final @NonNull List<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            sb.append(fieldName(it.next()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * Template method which generates method parameters with their types from {@code parameters}.
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in Java format
     */
    final @NonNull String asArgumentsDeclaration(final @NonNull Iterable<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedName(parameter.getReturnType())).append(' ').append(fieldName(parameter));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>, annotating them
     * with {@link NonNull}.
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    final @NonNull String asNonNullArgumentsDeclaration(final @NonNull List<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedNonNull(parameter.getReturnType())).append(' ').append(fieldName(parameter));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    static final @NonNull String getterMethodName(final @NonNull String propName) {
        return Naming.GETTER_PREFIX + Naming.toFirstUpper(propName);
    }

    static final @NonNull String getterMethodName(final GeneratedProperty field) {
        return getterMethodName(field.getName());
    }

    /**
     * Return properties participating in the construction of a key type. Returned list is guaranteed to be ordered to
     * match order the type constructor expects.
     *
     * @param keyType key type
     * @return properties participating in the construction of a key type, in constructor order
     */
    static final @NonNull List<GeneratedProperty> keyConstructorArgs(final GeneratedTransferObject keyType) {
        return keyType.getProperties().stream()
            .sorted(Comparator.comparing(GeneratedProperty::getName))
            .collect(Collectors.toList());
    }

    @NonNullByDefault
    static final String wrapToDocumentation(final String text) {
        if (text.isEmpty()) {
            return text;
        }

        final var sb = new StringBuilder().append("/**\n");
        for (var line : NL_SPLITTER.split(text)) {
            sb.append(" *");
            if (!line.isEmpty()) {
                sb.append(' ').append(line);
            }
            sb.append('\n');
        }
        return sb.append(" */").toString();
    }
}
