/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.contract.Naming.BUILDER_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.NONNULL_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.entryObject;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.lib.AbstractAugmentable;
import org.opendaylight.yangtools.binding.lib.AbstractDataContainer;
import org.opendaylight.yangtools.binding.lib.AbstractEntryObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;

/**
 * A template for the inner implementation class supported by a {@link BuilderTemplate}.
 */
// FIXME: consider refactoring as an inner class in BuilderTemplate, as we are never a standalone template, which
//        would allow proper specialization based on properties.isEmpty(), augmentType != null and keyType != null.
final class BuilderImplTemplate extends BaseTemplate {
    /**
     * {@link AbstractDataContainer} as a {@link JavaTypeName}.
     */
    private static final @NonNull JavaTypeName ABSTRACT_DATA_CONTAINER =
        JavaTypeName.create(AbstractDataContainer.class);
    /**
     * {@link AbstractAugmentable} as a {@link JavaTypeName}.
     */
    private static final @NonNull JavaTypeName ABSTRACT_AUGMENTABLE =
        JavaTypeName.create(AbstractAugmentable.class);
    /**
     * {@link AbstractEntryObject} as a {@link JavaTypeName}.
     */
    private static final @NonNull JavaTypeName ABSTRACT_ENTRY_OBJECT = JavaTypeName.create(AbstractEntryObject.class);

    private final @NonNull BuilderTemplate builder;

    @NonNullByDefault
    BuilderImplTemplate(final BuilderTemplate builder, final GeneratedType type) {
        // FIXME: we should be delegating access to these fields to builder
        super(builder.javaType().getNestedClass(type), type);
        this.builder = builder;
    }

    @Override
    BlockBuilder body() {
        // cache things from builder
        final var targetType = builder.targetType;
        final var keyType = builder.keyType;
        final var augmentType = builder.augmentType;
        final var properties = builder.properties;
        final var builderType = builder.type();

        final var type = type();
        final var implIface = importedName(targetType);
        final var override = importedName(OVERRIDE);
        final var simpleName = type.simpleName();

        final var bb = newBlockBuilder();

        for (var annotation : targetType.getAnnotations()) {
            if (JavaFileTemplate.DEPRECATED.equals(annotation.name())) {
                bb.blk(generateAnnotation(annotation));
                break;
            }
        }

        bb
            .str("private static final class ").str(simpleName).str(" extends ");
        if (keyType != null) {
            // EntryObject
            bb.gen(importedName(ABSTRACT_ENTRY_OBJECT), implIface, importedName(keyType));
        } else {
            bb.gen(augmentType == null
                // Augmentation, YangData
                ? importedName(ABSTRACT_DATA_CONTAINER)
                // everything else
                : importedName(ABSTRACT_AUGMENTABLE), implIface);
        }
        bb.str(" implements ").str(implIface).oB();

        // generate instance fields
        if (!properties.isEmpty()) {
            for (var prop : properties) {
                bb.str("private final ").str(importedReturnType(prop)).sp().str(fieldName(prop)).eS();
            }
        }

        bb
            .nl()
            .str(simpleName).str("(final ").str(importedName(builderType)).str(" base)").oB();

        if (augmentType != null) {
            bb.str("super(base." + BuilderTemplate.AUGMENTATION_FIELD);
            if (keyType != null) {
                bb.str(", extractKey(base)");
            }
            bb.eol(");");
        }

        if (keyType != null && targetType.getImplements().contains(entryObject(targetType, keyType))) {
            final var allProps = new ArrayList<>(properties);
            final var keyProps = AbstractBuilderTemplate.keyConstructorArgs(keyType);
            for (var field : keyProps) {
                AbstractBuilderTemplate.removeProperty(allProps, field.getName());
            }

            bb.eol("final var key = key();");
            for (var field : keyProps) {
                bb.str("this.").str(fieldName(field)).str(" = key.").str(getterMethodName(field)).eol("();");
            }

            appendCopyNonKeys(bb, allProps);
        } else {
            appendCopyNonKeys(bb, properties);
        }

        bb.cB();

        if (keyType != null) {
            // TODO: this is generating a utility static method for use in the (only) constructor. We should be inlining
            //       this code into the constructor once JEP-482 Flexible Constructor Bodies available. We should
            //       construct the key into a 'key' local variable, so that generateCopyKeys() below can reference it
            bb
                .nl()
                .str("private static ").str(importedNonNull(keyType)).str(" extractKey(final ")
                    .str(importedName(builderType)).str(" base)").oB()
                        .str("final var key = base." + KEY_AWARE_KEY_NAME).eol("();")
                        .eol("return key != null ? key")
                        .str("    : new ").str(importedName(keyType)).str("(");

            // Note: keys have at least one component
            final var it = AbstractBuilderTemplate.keyConstructorArgs(keyType).iterator();
            while (true) {
                final var keyProp = it.next();
                bb.str("base.").str(getterMethodName(keyProp)).str("()");
                if (!it.hasNext()) {
                    break;
                }
                bb.str(", ");
            }

            bb
                .eol(");")
                .cB();
        }

        // generate getters
        if (!properties.isEmpty()) {
            bb.newLine();

            final var it = properties.iterator();
            while (true) {
                final var field = it.next();

                // getFoo()
                bb.blk(asGetterMethod(field));

                // nonnullFoo() for structural containers
                if (field.getReturnType() instanceof GeneratedType fieldType
                    && AbstractBuilderTemplate.isNonPresenceContainer(fieldType)) {
                    bb
                        .nl()
                        .at().eol(override)
                        .str("public ").str(importedName(fieldType)).str(" " + NONNULL_PREFIX)
                            .str(toFirstUpper(field.getName())).str("()").oB()
                            .str("var tmp = ").str(getterMethodName(field)).eol("();")
                            .str("return tmp != null ? tmp : ")
                                // FIXME: better reference to FooBuilder.empty()
                                .str(fieldType.canonicalName()).eol(BUILDER_SUFFIX + ".empty();")
                        .cB();
                }

                if (!it.hasNext()) {
                    break;
                }
                bb.newLine();
            }
        }

        return bb.cB();
    }

    @Override
    BlockBuilder asGetterMethod(final GeneratedProperty field) {
        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("public ").str(importedReturnType(field)).sp().str(getterMethodName(field)).str("()").jBlock(bb -> {
                final var fieldName = fieldName(field);
                if (field.getReturnType().isArray()) {
                    bb.str("return ").str(importedName(CODEHELPERS)).str(".copyArray(").str(fieldName).eol(");");
                } else {
                    bb.str("return ").str(fieldName).eS();
                }
            }).nl();
    }

    private void appendCopyNonKeys(final BlockBuilder bb, final Collection<BuilderGeneratedProperty> props) {
        for (var field : props) {
            bb.str("this.").str(fieldName(field)).str(" = ");

            if (field.getMechanics() == ValueMechanics.NULLIFY_EMPTY) {
                bb.str(importedName(CODEHELPERS)).str(".emptyToNull(base.").str(field.getGetterName()).eol("());");
            } else {
                bb.str("base.").str(field.getGetterName()).eol("();");
            }
        }
    }
}
