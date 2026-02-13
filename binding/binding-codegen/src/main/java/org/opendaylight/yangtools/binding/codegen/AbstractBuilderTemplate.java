/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

// FIXME: YANGTOOLS-1619: convert to Java
abstract class AbstractBuilderTemplate extends BaseTemplate {
    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME.
     */
    protected final Type augmentType;

    /**
     * Set of class attributes (fields) which are derived from the getter methods names.
     */
    protected final Set<BuilderGeneratedProperty> properties;

    /**
     * GeneratedTransferObject for key type, {@code null} if this type does not have a key.
     */
    protected final GeneratedTransferObject keyType;

    protected final GeneratedType targetType;

    AbstractBuilderTemplate(final AbstractJavaGeneratedType javaType, final GeneratedType type,
            final GeneratedType targetType, final Set<BuilderGeneratedProperty> properties, final Type augmentType,
            final GeneratedTransferObject keyType) {
        super(javaType, type);
        this.targetType = targetType;
        this.properties = properties;
        this.augmentType = augmentType;
        this.keyType = keyType;
    }

    AbstractBuilderTemplate(final GeneratedType type, final GeneratedType targetType,
            final GeneratedTransferObject keyType) {
        super(type);
        this.targetType = targetType;
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
    final String generateFields(final boolean makeFinal) {
        final var sb = new StringBuilder();
        if (properties != null) {
            for (var f : properties) {
                sb.append("private ");
                if (makeFinal) {
                    sb.append("final ");
                }
                sb.append(importedName(f.getReturnType())).append(' ').append(fieldName(f)).append(";\n");
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
    // FIXME: specialize to the two possibilities? should make the code more readable
    final String generateGetters(final boolean addOverride) {
        final var sb = new StringBuilder();
        if (keyType != null) {
            if (!addOverride) {
                sb.append("""
                    /**
                     * Return current value associated with the property corresponding to {@link \
                    """).append(importedName(targetType)).append('#').append(Naming.KEY_AWARE_KEY_NAME).append("()}.\n")
                    .append("""
                     *
                     * @return current value
                     */
                    """);
            } else {
                sb.append('@').append(importedName(OVERRIDE)).append('\n');
            }
            sb.append("public ").append(importedName(keyType)).append(' ').append(Naming.KEY_AWARE_KEY_NAME).append("""
                () {
                    return key;
                }
                """)
                .append("\n");
        }

        final var it = properties.iterator();
        if (!it.hasNext()) {
            return sb.toString();
        }

        while (true) {
            appendGetter(sb, it.next(), addOverride);
            if (!it.hasNext()) {
                return sb.toString();
            }

            sb.append('\n');
        }
    }

    private void appendGetter(final StringBuilder sb, final BuilderGeneratedProperty field, final boolean addOverride) {
        if (!addOverride) {
            sb.append("""
                /**
                 * Return current value associated with the property corresponding to {@link \
                """).append(importedName(targetType)).append('#').append(field.getGetterName()).append("()}.\n")
                .append("""
                 *
                 * @return current value
                 */
                """);
        } else {
            sb.append('@').append(importedName(OVERRIDE)).append('\n');
        }
        sb.append(getterMethod(field));
    }

    final String generateCopyConstructor(final Type fromType, final Type implType) {
        final var sb = new StringBuilder().append(type().getName()).append("(final ").append(importedName(fromType))
            .append(" base) {\n");

        if (augmentType != null) {
            sb.append(generateCopyAugmentation(implType));
        }

        if (keyType != null && implementsIfc(targetType, BindingTypes.entryObject(targetType, keyType))) {
            final var keyProps = keyConstructorArgs(keyType);
            final var allProps = new ArrayList<>(properties);
            for (var field : keyProps) {
                removeProperty(allProps, field.getName());
            }
            sb.append(generateCopyKeys(keyProps));
            sb.append(generateCopyNonKeys(allProps));
        } else {
            sb.append(generateCopyNonKeys(properties));
        }

        return sb.append("\n}\n").toString();
    }

    final CharSequence generateDeprecatedAnnotation(final List<AnnotationType> annotations) {
        final var found = findDeprecatedAnnotation(annotations);
        return found == null ? "" : generateDeprecatedAnnotation(found);
    }

    abstract CharSequence generateDeprecatedAnnotation(AnnotationType ann);

    abstract CharSequence generateCopyKeys(List<GeneratedProperty> keyProps);

    abstract CharSequence generateCopyNonKeys(Collection<BuilderGeneratedProperty> props);

    abstract CharSequence generateCopyAugmentation(Type implType);

    private static boolean implementsIfc(final GeneratedType type, final Type impl) {
        for (var ifc : type.getImplements()) {
            if (ifc.equals(impl)) {
                return true;
            }
        }
        return false;
    }

    private void removeProperty(final Collection<BuilderGeneratedProperty> props, final String name) {
        final var iter = props.iterator();
        while (iter.hasNext()) {
            if (name.equals(iter.next().getName())) {
                iter.remove();
                return;
            }
        }
    }

    private static @Nullable AnnotationType findDeprecatedAnnotation(final List<AnnotationType> annotations) {
        if (annotations != null) {
            for (var annotation : annotations) {
                if (DEPRECATED.equals(annotation.getIdentifier())) {
                    return annotation;
                }
            }
        }
        return null;
    }

    static final boolean hasNonDefaultMethods(final GeneratedType type) {
        final var defs = type.getMethodDefinitions();
        return !defs.isEmpty() && defs.stream().anyMatch(def -> !def.isDefault());
    }

    static final Collection<MethodSignature> nonDefaultMethods(final GeneratedType type) {
        return Collections2.filter(type.getMethodDefinitions(), def -> !def.isDefault());
    }
}
