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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

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

    /**
     * Template method which generates the getter method for {@code field}.
     *
     * @param field generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    @NonNullByDefault
    CharSequence asGetterMethod(final GeneratedProperty field) {
        // derive state
        final var fieldName = fieldName(field);
        final var methodName = getterMethodName(field);
        final var returnType = field.getReturnType();
        final var importedName = importedName(returnType);
        // any Java array type needs to be duplicated to prevent modification
        final var copy = returnType.getName().endsWith("[]");

        // emit separately
        final var sb = new StringBuilder()
            .append("public ").append(importedName).append(' ').append(methodName).append("() {\n")
            .append("    return ").append(fieldName);
        // FIXME: offload this logic to CodeHelpers: this should only apply to byte[] and therefore we can just call
        //        CodeHelpers.isolatedBytes(byte[]);
        if (copy) {
            sb.append(" == null ? null : ").append(fieldName).append(".clone()");
        }
        return sb.append(";\n}\n").toString();
    }

    /**
     * Template method which generates the setter method for {@code field}.
     *
     * @param field generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format
     */
    @NonNullByDefault
    final CharSequence asSetterMethod(final GeneratedProperty field) {
        final var fieldName = fieldName(field);
        final var fieldType = importedName(field.getReturnType());
        final var suffix = StringExtensions.toFirstUpper(field.getName());
        final var typeName = type().getName();

        return new StringBuilder()
            .append("public ").append(typeName).append(" set").append(suffix).append('(').append(fieldType)
                .append(" value) {\n")
            .append("    this.").append(fieldName).append(" = value;\n")
            .append("    return this;\n")
            .append("}\n")
            .toString();
    }

    /**
     * Template method which generates JAVA comments.
     *
     * @param comment string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    static final @NonNull String asJavadoc(final @Nullable TypeMemberComment comment) {
        if (comment == null) {
            return "";
        }

        final var sb = new StringBuilder();
        final var contract = comment.contractDescription();
        if (contract != null) {
            sb.append(contract).append("\n\n");
        }
        final var reference = comment.referenceDescription();
        if (reference != null) {
            sb.append(BaseTemplate.formatReference(reference));
        }
        final var signature = comment.typeSignature();
        if (signature != null) {
            sb.append(signature).append('\n');
        }
        return wrapToDocumentation(sb.toString());
    }

    @NonNullByDefault
    String formatDataForJavaDoc(final GeneratedType type) {
        final var sb = new StringBuilder();
        final var comment = type.getComment();
        if (comment != null) {
            sb.append(comment.getJavadoc());
        }
        appendSnippet(sb, type);

        final var str = sb.toString();
        return str.isBlank() ? "" : str.stripTrailing() + '\n';
    }

    @NonNullByDefault
    final String formatDataForJavaDoc(final GeneratedType type, final String additionalComment) {
        final var comment = type.getComment();
        if (comment == null) {
            return additionalComment.isBlank() ? "" : additionalComment.stripTrailing() + '\n';
        }

        final var sb = new StringBuilder().append(comment.getJavadoc());
        appendSnippet(sb, type);
        return additionalComment.isBlank() ? sb.toString()
            : sb.append(additionalComment.stripTrailing()).append("\n\n\n").toString();
    }

    @NonNullByDefault
    final String generateAnnotation(final AnnotationType annotation) {
        final var sb = new StringBuilder()
            .append('@').append(importedName(annotation));

        final var params = annotation.getParameters();
        if (params != null && !params.isEmpty()) {
            sb.append("(\n");

            final var it = params.iterator();
            while (true) {
                final var param = it.next();
                sb.append("    ").append(param.getName()).append('=').append(param.getValue());
                if (!it.hasNext()) {
                    break;
                }
                sb.append(",\n");
            }

            sb.append("\n)");
        }

        return sb.append('\n').toString();
    }

    @NonNullByDefault
    final String generateCheckers(final GeneratedProperty field, final Restrictions restrictions,
            final Type actualType) {
        final var sb = new StringBuilder();
        restrictions.getRangeConstraint().ifPresent(
            range -> sb
                .append(AbstractRangeGenerator.forType(actualType)
                    .generateRangeChecker(StringExtensions.toFirstUpper(field.getName()), range, this))
        );
        // FIXME: this call looks unlike the range checker call: it should be refactored to acquire a generator,
        //        so that we can suppress checker when not needed -- just like ranges do above
        restrictions.getLengthConstraint().ifPresent(
            length -> sb.append(LengthGenerator.generateLengthChecker(fieldName(field), actualType, length, this))

        );
        return sb.toString();
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
