/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

abstract class AbstractBuilderTemplate extends BaseTemplate {
    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME.
     */
    final Type augmentType;

    /**
     * Set of class attributes (fields) which are derived from the getter methods names.
     */
    final Set<BuilderGeneratedProperty> properties;

    /**
     * GeneratedTransferObject for key type, {@code null} if this type does not have a key.
     */
    final GeneratedTransferObject keyType;

    final GeneratedType targetType;

    AbstractBuilderTemplate(final @NonNull AbstractJavaGeneratedType javaType, final @NonNull GeneratedType type,
        final GeneratedType targetType, final Set<BuilderGeneratedProperty> properties, final Type augmentType,
        final GeneratedTransferObject keyType) {
        super(javaType, type);
        this.targetType = targetType;
        this.properties = properties;
        this.augmentType = augmentType;
        this.keyType = keyType;
    }

    AbstractBuilderTemplate(final @NonNull GeneratedType type, final @NonNull GeneratedType targetType,
        final GeneratedTransferObject keyType) {
        super(type);
        this.targetType = requireNonNull(targetType);
        this.keyType = keyType;
        final var analysis = analyzeTypeHierarchy(targetType);
        augmentType = analysis.getKey();
        properties = analysis.getValue();
    }

    /**
     * Template method which generates class attributes.
     *
     * @param makeFinal value which specify whether field is|isn't final
     * @return string with class attributes and their types
     */
    // FIXNE: declareFields() and declareFinalFields() are two distinct operations
    final String generateFields(final boolean makeFinal) {
        final var sb = new StringBuilder();
        if (properties != null) {
            for (var prop : properties) {
                sb.append("private ");
                if (makeFinal) {
                    sb.append("final ");
                }
                sb.append(importedName(prop.getReturnType())).append(' ').append(fieldName(prop)).append(";\n");
            }
        }
        if (keyType != null && !makeFinal) {
            sb.append("private ").append(importedName(keyType)).append(" key;\n");
        }
        return sb.toString();
    }

    /**
     * Template method which generate getter methods for IMPL class.
     *
     * @return string with getter methods
     */
    final CharSequence generateGetters(final boolean addOverride) {
        final var sb = new StringBuilder();

        if (keyType != null) {
            if (!addOverride) {
                sb
                    .append("/**\n")
                    .append(" * Return current value associated with the property corresponding to {@link ")
                        .append(importedName(targetType)).append('#').append(Naming.KEY_AWARE_KEY_NAME)
                        .append("()}.\n")
                    .append(" *\n")
                    .append(" * @return current value\n")
                    .append(" */\n");
            } else {
                sb
                    .append('@').append(importedName(OVERRIDE)).append('\n');
            }
            sb
                .append("public ").append(importedName(keyType)).append(' ').append(Naming.KEY_AWARE_KEY_NAME)
                    .append("() {\n")
                .append("    return key;\n")
                .append("}\n\n");
        }

        if (properties.isEmpty()) {
            return sb.toString();
        }

        final var it = properties.iterator();
        while (true) {
            final var field = it.next();
            if (!addOverride) {
                sb
                    .append("/**\n")
                    .append(" * Return current value associated with the property corresponding to {@link ")
                        .append(importedName(targetType)).append('#').append(field.getGetterName()).append("()}.\n")
                    .append(" *\n")
                    .append(" * @return current value\n")
                    .append(" */\n");
            } else {
                sb
                    .append("@").append(importedName(OVERRIDE)).append('\n');
            }
            sb.append(asGetterMethod(field));

            if (!it.hasNext()) {
                return sb.toString();
            }

            sb.append('\n');
        }
    }

    final String generateCopyConstructor(final Type fromType, final Type implType) {
        final var sb = new StringBuilder()
            .append(type().simpleName()).append("(final ").append(importedName(fromType)).append(" base) {\n");

        if (augmentType != null) {
            // FIXME: create 'appendCopyAugmentation' method for use here
            final var sc = new StringConcatenation();
            sc.append("    ");
            sc.append(generateCopyAugmentation(implType), "    ");
            sc.newLineIfNotEmpty();
            sb.append(sc);
        }

        final var sc = new StringConcatenation();
        if (keyType != null && targetType.getImplements().contains(BindingTypes.entryObject(targetType, keyType))) {
            final var allProps = new ArrayList<>(properties);
            final var keyProps = BaseTemplate.keyConstructorArgs(keyType);
            for (var field : keyProps) {
                removeProperty(allProps, field.getName());
            }
            sc.append("    ");
            sc.append(generateCopyKeys(keyProps), "    ");
            sc.newLineIfNotEmpty();
            sc.append("    ");
            sc.append(generateCopyNonKeys(allProps), "    ");
            sc.newLineIfNotEmpty();
        } else {
            sc.append("    ");
            sc.append(generateCopyNonKeys(properties), "    ");
            sc.newLineIfNotEmpty();
        }

        return sb.append(sc).append("}\n").toString();
    }

    // FIXME: 'append' alternative to this
    abstract CharSequence generateCopyKeys(List<GeneratedProperty> keyProps);

    // FIXME: 'append' alternative to this
    abstract CharSequence generateCopyNonKeys(Collection<BuilderGeneratedProperty> props);

    // FIXME: 'append' alternative to this
    abstract CharSequence generateCopyAugmentation(Type implType);

    final CharSequence generateDeprecatedAnnotation(final @Nullable List<AnnotationType> annotations) {
        if (annotations != null) {
            for (var annotation : annotations) {
                if (JavaFileTemplate.DEPRECATED.equals(annotation.name())) {
                    return generateDeprecatedAnnotation(annotation);
                }
            }
        }
        return "";
    }

    abstract CharSequence generateDeprecatedAnnotation(@NonNull AnnotationType ann);

    private static void removeProperty(final Collection<BuilderGeneratedProperty> props, final String name) {
        final var it = props.iterator();
        while (it.hasNext()) {
            if (name.equals(it.next().getName())) {
                it.remove();
                return;
            }
        }
    }

    @NonNullByDefault
    static final boolean hasNonDefaultMethods(final GeneratedType type) {
        return type.getMethodDefinitions().stream().anyMatch(def -> !def.isDefault());
    }

    @NonNullByDefault
    static final Collection<MethodSignature> nonDefaultMethods(final GeneratedType type) {
        return Collections2.filter(type.getMethodDefinitions(), def -> !def.isDefault());
    }
}
