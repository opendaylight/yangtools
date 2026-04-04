/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.contract.Naming.AUGMENTATION_FIELD;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_EQUALS_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_HASHCODE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_TO_STRING_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.BUILDER_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.NONNULL_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;

import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.lib.AbstractAugmentable;
import org.opendaylight.yangtools.binding.lib.AbstractEntryObject;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * A template for the inner implementation class supported by a {@link BuilderTemplate}.
 */
// FIXME: consider refactoring as an inner class in BuilderTemplate, as we are never a standalone template, which
//        would allow proper specialization based on properties.isEmpty(), augmentType != null and keyType != null.
final class BuilderImplTemplate extends AbstractBuilderTemplate {
    /**
     * {@link AbstractAugmentable} as a {@link JavaTypeName}.
     */
    private static final @NonNull JavaTypeName ABSTRACT_AUGMENTABLE = JavaTypeName.create(AbstractAugmentable.class);
    /**
     * {@link AbstractEntryObject} as a {@link JavaTypeName}.
     */
    private static final @NonNull JavaTypeName ABSTRACT_ENTRY_OBJECT = JavaTypeName.create(AbstractEntryObject.class);

    private final @NonNull BuilderTemplate builder;

    @NonNullByDefault
    BuilderImplTemplate(final BuilderTemplate builder, final GeneratedType type) {
        // FIXME: pass builder to super?
        super(builder.javaType().getNestedClass(type), type, builder.targetType, builder.properties,
            builder.augmentType, builder.keyType);
        this.builder = builder;
    }

    @Override
    BlockBuilder body() {
        final var impIface = importedName(targetType);
        final var override = importedName(OVERRIDE);

        final var bb = newBlockBuilder()
            .blk(generateDeprecatedAnnotation(targetType.getAnnotations()))
            .str("private static final class ").eol(type().simpleName());
        if (keyType != null) {
            bb.str("    extends ").gen(importedName(ABSTRACT_ENTRY_OBJECT), impIface, importedName(keyType)).newLine();
        } else if (augmentType != null) {
            bb.str("    extends ").gen(importedName(ABSTRACT_AUGMENTABLE), impIface).newLine();
        }
        bb.str("    implements ").str(impIface).oB();

        // generate instance fields
        if (!properties.isEmpty()) {
            bb.newLine();
            for (var prop : properties) {
                bb.str("private final ").str(importedReturnType(prop)).sp().str(fieldName(prop)).eS();
            }
        }

        bb
            .nl()
            .indented(generateCopyConstructor(builder.type(), type()));

        if (keyType != null) {
            // TODO: this is generating a utility static method for use in the (only) constructor. We should be inlining
            //       this code into the constructor once JEP-482 Flexible Constructor Bodies available. We should
            //       construct the key into a 'key' local variable, so that generateCopyKeys() below can reference it
            bb
                .nl()
                .str("private static ").str(importedNonNull(keyType)).str(" extractKey(final ")
                    .str(importedName(builder.type())).str(" base)").oB()
                        .str("final var key = base." + KEY_AWARE_KEY_NAME).eol("();")
                        .eol("return key != null ? key")
                        .str("    : new ").str(importedName(keyType)).str("(");

            // Note: keys have at least one component
            final var it = keyConstructorArgs(keyType).iterator();
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
                bb.indented(asGetterMethod(field));

                // nonnullFoo() for structural containers
                if (field.getReturnType() instanceof GeneratedType fieldType && isNonPresenceContainer(fieldType)) {
                    bb
                        .nl()
                        .ind("@").eol(override)
                        .ind("public ").str(importedName(fieldType)).str(" " + NONNULL_PREFIX)
                            .str(toFirstUpper(field.getName())).str("()").oB()
                            .str("return ").str(importedName(JU_OBJECTS)).str(".requireNonNullElse(")
                                .str(getterMethodName(field)).str("(), ").str(fieldType.canonicalName())
                                .eol(BUILDER_SUFFIX + ".empty());")
                        .cB();
                }

                if (!it.hasNext()) {
                    break;
                }
                bb.newLine();
            }
        }

        // generate hashCode/equals if needed
        if (!properties.isEmpty() || augmentType != null) {
            bb
                .txt("""

                          private int hash = 0;
                          private volatile boolean hashValid = false;

                      """)
                .str("    @").eol(override)
                .txt("""
                          public int hashCode() {
                              if (hashValid) {
                                  return hash;
                              }

                      """)
                .str("        final int result = ").str(impIface).eol("." + BINDING_HASHCODE_NAME + "(this);")
                .txt("""
                              hash = result;
                              hashValid = true;
                              return result;
                          }

                      """)
                .str("    @").eol(override)
                .str("    public boolean equals(").str(importedName(Types.objectType())).str(" obj)").oB()
                .str("        return ").str(impIface).eol("." + BINDING_EQUALS_NAME + "(this, obj);")
                .str("    ").cB();
        }

        // generate equals()
        return bb
            .nl()
            .str("    @").eol(override)
            .str("    public ").str(importedName(Types.STRING)).str(" toString()").oB()
            .str("        return ").str(impIface).eol("." + BINDING_TO_STRING_NAME + "(this);")
            .str("    ").cB()
            .cB();
    }

    @Override
    BlockBuilder generateDeprecatedAnnotation(final AnnotationType ann) {
        return generateAnnotation(ann);
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

    @NonNullByDefault
    MethodSignature findGetter(final String getterName) {
        final var type = type();
        final var getter = getterByName(type, getterName);
        if (getter == null) {
            throw new IllegalStateException(
                "%s should be present in %s type or in one of its ancestors as getter".formatted(
                    propertyNameFromGetter(getterName), type));
        }
        return getter;
    }

    private static @Nullable MethodSignature getterByName(final GeneratedType implType, final String getterName) {
        final var getter = getterByName(nonDefaultMethods(implType), getterName);
        if (getter != null) {
            return getter;
        }
        for (var ifc : implType.getImplements()) {
            if (ifc instanceof GeneratedType genInterface) {
                final var getterImpl = getterByName(genInterface, getterName);
                if (getterImpl != null) {
                    return getterImpl;
                }
            }
        }

        return null;
    }

    @Override
    void appendCopyKeys(final BlockBuilder bb, final List<GeneratedProperty> keyProps) {
        bb.eol("final var key = key();");
        for (var field : keyProps) {
            bb.str("this.").str(fieldName(field)).str(" = key.").str(getterMethodName(field)).eol("();");
        }
    }

    @Override
    void appendCopyNonKeys(final BlockBuilder bb, final Collection<BuilderGeneratedProperty> props) {
        for (var field : props) {
            bb.str("this.").str(fieldName(field)).str(" = ");

            if (field.getMechanics() == ValueMechanics.NULLIFY_EMPTY) {
                bb.str(importedName(CODEHELPERS)).str(".emptyToNull(base.").str(field.getGetterName()).eol("());");
            } else {
                bb.str("base.").str(field.getGetterName()).eol("();");
            }
        }
    }

    @Override
    void appendCopyAugmentation(final BlockBuilder bb) {
        bb.str("super(base." + AUGMENTATION_FIELD);
        if (keyType != null) {
            bb.str(", extractKey(base)");
        }
        bb.eol(");");
    }
}
