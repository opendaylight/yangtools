/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.entryObject;

import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

abstract sealed class AbstractBuilderTemplate extends BaseTemplate permits BuilderTemplate, BuilderImplTemplate {
    /**
     * Generated property is set if among methods is found one with the name GET_AUGMENTATION_METHOD_NAME.
     */
    final ParameterizedType augmentType;

    /**
     * Set of class attributes (fields) which are derived from the getter methods names.
     */
    final Set<BuilderGeneratedProperty> properties;

    /**
     * KeyArchetype for key type, {@code null} if this type does not have a key.
     */
    final KeyArchetype keyType;

    // FIXME: better description: 'targetType' in the context of BuilderImplTemplate is type returned
    //        from BindingContract.implementedInterface() -- and is expected to extend JavaContract and provide default
    //        implementations of its methods
    final GeneratedType targetType;

    AbstractBuilderTemplate(final @NonNull GeneratedClass javaType, final @NonNull GeneratedType type,
            final GeneratedType targetType, final Set<BuilderGeneratedProperty> properties,
            final ParameterizedType augmentType, final KeyArchetype keyType) {
        super(javaType, type);
        this.targetType = targetType;
        this.properties = properties;
        this.augmentType = augmentType;
        this.keyType = keyType;
    }

    AbstractBuilderTemplate(final @NonNull GeneratedType type, final @NonNull GeneratedType targetType,
            final KeyArchetype keyType) {
        super(GeneratedClass.of(type), type);
        this.targetType = requireNonNull(targetType);
        this.keyType = keyType;
        final var analysis = TypeAnalysis.of(targetType);
        properties = analysis.properties();
        augmentType = analysis.augmentType();
    }

    /**
     * Template method which generate getter methods for IMPL class.
     *
     * @return string with getter methods
     */
    final @NonNull BlockBuilder generateGetters(final boolean addOverride) {
        final var bb = newBlockBuilder();

        if (keyType != null) {
            if (!addOverride) {
                bb
                    .eol("/**")
                    .str(" * Return current value associated with the property corresponding to {@link ")
                        .str(importedName(targetType)).eol('#' + KEY_AWARE_KEY_NAME + "()}.")
                    .eol(" *")
                    .eol(" * @return current value")
                    .eol(" */");
            } else {
                bb
                    .at().eol(importedName(OVERRIDE));
            }
            bb
                .str("public ").str(importedName(keyType)).str(' ' + KEY_AWARE_KEY_NAME + "()").oB()
                    .eol("return key;")
                .cB()
                .newLine();
        }

        if (properties.isEmpty()) {
            return bb;
        }

        final var it = properties.iterator();
        while (true) {
            final var field = it.next();
            if (!addOverride) {
                bb
                    .eol("/**")
                    .str(" * Return current value associated with the property corresponding to {@link ")
                        .str(importedName(targetType)).str("#").str(field.getGetterName()).eol("()}.")
                    .eol(" *")
                    .eol(" * @return current value")
                    .eol(" */");
            } else {
                bb
                    .at().eol(importedName(OVERRIDE));
            }
            bb.blk(asGetterMethod(field));

            if (!it.hasNext()) {
                return bb;
            }

            bb.newLine();
        }
    }

    @NonNullByDefault
    final BlockBuilder generateCopyConstructor(final Type fromType, final Type implType) {
        return newBlockBuilder()
            .str(type().simpleName()).str("(final ").str(importedName(fromType)).str(" base)").jBlock(bb -> {
                if (augmentType != null) {
                    appendCopyAugmentation(bb);
                }

                if (keyType != null && targetType.getImplements().contains(entryObject(targetType, keyType))) {
                    final var allProps = new ArrayList<>(properties);
                    final var keyProps = keyConstructorArgs(keyType);
                    for (var field : keyProps) {
                        removeProperty(allProps, field.getName());
                    }

                    appendCopyKeys(bb, keyProps);
                    appendCopyNonKeys(bb, allProps);
                } else {
                    appendCopyNonKeys(bb, properties);
                }
            }).nl();
    }

    /**
     * Return properties participating in the construction of a key type. Returned list is guaranteed to be ordered to
     * match order the type constructor expects.
     *
     * @param keyType key type
     * @return properties participating in the construction of a key type, in constructor order
     */
    @NonNullByDefault
    static final List<GeneratedProperty> keyConstructorArgs(final KeyArchetype keyType) {
        return keyType.getProperties().stream()
            .sorted(Comparator.comparing(GeneratedProperty::getName))
            .collect(Collectors.toList());
    }

    /**
     * Append the code to copy key components, with four spaces of indentation.
     */
    @NonNullByDefault
    abstract void appendCopyKeys(BlockBuilder bb, List<GeneratedProperty> keyProps);

    /**
     * Append the code to copy non-key-components, with four spaces of indentation.
     */
    abstract void appendCopyNonKeys(BlockBuilder bb, Collection<BuilderGeneratedProperty> props);

    /**
     * Append the code to copy augmentations from a {@code base} local variable, with four spaces of indentation.
     */
    abstract void appendCopyAugmentation(BlockBuilder bb);

    final @Nullable BlockBuilder generateDeprecatedAnnotation(final @Nullable List<AnnotationType> annotations) {
        if (annotations != null) {
            for (var annotation : annotations) {
                if (JavaFileTemplate.DEPRECATED.equals(annotation.name())) {
                    return generateDeprecatedAnnotation(annotation);
                }
            }
        }
        return null;
    }

    abstract BlockBuilder generateDeprecatedAnnotation(@NonNull AnnotationType ann);

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

    /**
     * Check if the {@code type} represents non-presence container.
     *
     * @param type {@link GeneratedType} to be checked if represents container without presence statement.
     * @return {@code true} if specified {@code type} is a container without presence statement,
     *     {@code false} otherwise.
     */
    // FIXME: YANGTOOLS-1876: remove this method
    @NonNullByDefault
    static final boolean isNonPresenceContainer(final GeneratedType type) {
        if (type instanceof Archetype) {
            return false;
        }
        final var sourceDef = type.yangSourceDefinition();
        return sourceDef != null && sourceDef.getNode() instanceof ContainerSchemaNode container
            && !container.isPresenceContainer();
    }
}
