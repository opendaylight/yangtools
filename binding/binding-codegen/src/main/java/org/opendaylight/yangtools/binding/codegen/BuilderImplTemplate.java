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

    BuilderImplTemplate(final BuilderTemplate builder, final GeneratedType type) {
        super(builder.javaType().getEnclosedType(type.name()), type, builder.targetType, builder.properties,
            builder.augmentType, builder.keyType);
        this.builder = builder;
    }

    @Override
    public CharSequence body() {
        final var impIface = importedName(targetType);
        final var override = importedName(OVERRIDE);

        final var bb = new BlockBuilder();
        bb.append(generateDeprecatedAnnotation(targetType.getAnnotations()));
        bb.newLineIfNotEmpty();
        bb.str("private static final class ").str(type().simpleName()).newLineIfNotEmpty();
        if (keyType != null) {
            bb.str("    extends ").str(importedName(ABSTRACT_ENTRY_OBJECT)).str("<").str(impIface).str(", ")
                .str(importedName(keyType)).append(">\n");
        } else if (augmentType != null) {
            bb.str("    extends ").str(importedName(ABSTRACT_AUGMENTABLE)).str("<").str(impIface).append(">\n");
        }
        bb.str("    implements ").str(impIface).append(" {\n");

        // generate instance fields
        if (!properties.isEmpty()) {
            bb.newLine();
            for (var prop : properties) {
                bb.str("    private final ").str(importedReturnType(prop)).str(" ").str(fieldName(prop)).append(";\n");
            }
        }

        bb.nl().append("    ");
        bb.append(generateCopyConstructor(builder.type(), type()), "    ");
        bb.newLineIfNotEmpty();

        if (keyType != null) {
            // TODO: this is generating a utility static method for use in the (only) constructor. We should be inlining
            //       this code into the constructor once JEP-482 Flexible Constructor Bodies available. We should
            //       construct the key into a 'key' local variable, so that generateCopyKeys() below can reference it
            bb.nl().str("    private static ").str(importedNonNull(keyType)).str(" extractKey(final ")
                .str(importedName(builder.type())).append(" base) {\n");
            bb.str("        final var key = base." + KEY_AWARE_KEY_NAME).append("();\n");
            bb.append("        return key != null ? key\n");
            bb.str("            : new ").str(importedName(keyType)).append("(");

            // Note: keys have at least one component
            final var it = keyConstructorArgs(keyType).iterator();
            while (true) {
                final var keyProp = it.next();
                bb.str("base.").str(getterMethodName(keyProp)).append("()");
                if (!it.hasNext()) {
                    break;
                }
                bb.append(", ");
            }

            bb.append(");\n");
            bb.append("    }\n");
        }

        // generate getters
        if (!properties.isEmpty()) {
            bb.newLine();

            final var it = properties.iterator();
            while (true) {
                final var field = it.next();

                // getFoo()
                bb.append("    ");
                bb.append(asGetterMethod(field), "    ");

                // nonnullFoo() for structural containers
                if (field.getReturnType() instanceof GeneratedType fieldType && isNonPresenceContainer(fieldType)) {
                    bb.nl()
                        .str("    @").str(override).nl()
                        .str("    public ").str(importedName(fieldType)).str(" ")
                        .str(NONNULL_PREFIX).str(toFirstUpper(field.getName())).append("() {\n");
                    bb.str("        return ").str(importedName(JU_OBJECTS)).str(".requireNonNullElse(")
                        .str(getterMethodName(field)).str("(), ").str(fieldType.canonicalName()).str(BUILDER_SUFFIX)
                        .append(".empty());\n");
                    bb.append("}\n");
                }

                if (!it.hasNext()) {
                    break;
                }
                bb.newLine();
            }
        }

        // generate hashCode/equals if needed
        if (!properties.isEmpty() || augmentType != null) {
            bb.nl().append(
                      "    private int hash = 0;\n");
            bb.append("    private volatile boolean hashValid = false;\n");
            bb.nl().append(
                      "    @");
            bb.append(override);
            bb.nl().append(
                      "    public int hashCode() {\n");
            bb.append("        if (hashValid) {\n");
            bb.append("            return hash;\n");
            bb.append("        }\n");
            bb.nl().append(
                      "        final int result = ");
            bb.append(impIface);
            bb.str("." + BINDING_HASHCODE_NAME).append("(this);\n");
            bb.append("        hash = result;\n");
            bb.append("        hashValid = true;\n");
            bb.append("        return result;\n");
            bb.append("    }\n");
            bb.nl()
                .str("    @").str(override).nl()
                .str("    public boolean equals(").str(importedName(Types.objectType())).append(" obj) {\n");
            bb
                .str("        return ").str(impIface).str("." + BINDING_EQUALS_NAME).append("(this, obj);\n");
            bb.append("    }\n");
        }

        // generate equals()
        bb.nl().append(
                  "    @");
        bb.append(override);
        bb.nl().append(
                  "    public ");
        bb.append(importedName(Types.STRING));
        bb.append(" toString() {\n");
        bb.str("        return ").str(impIface).str("." + BINDING_TO_STRING_NAME).append("(this);\n");
        bb.append("    }\n");

        bb.append("}\n");
        return bb;
    }

    @Override
    public String generateDeprecatedAnnotation(final AnnotationType ann) {
        return generateAnnotation(ann);
    }

    @Override
    public String asGetterMethod(final GeneratedProperty field) {
        final var fieldName = fieldName(field);
        final var type = field.getReturnType();

        final var sb = new StringBuilder()
            .append('@').append(importedName(OVERRIDE)).append('\n')
            .append("public ").append(importedName(type)).append(' ').append(getterMethodName(field)).append("() {\n")
            .append("    return ");

        if (type.simpleName().endsWith("[]")) {
            sb.append(importedName(CODEHELPERS)).append(".copyArray(").append(fieldName).append(')');
        } else {
            sb.append(fieldName);
        }

        return sb.append(";\n").append("}\n").toString();
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
    void appendCopyKeys(final StringBuilder sb, final List<GeneratedProperty> keyProps) {
        sb.append("    final var key = key();\n");
        for (var field : keyProps) {
            sb.append("    this.").append(fieldName(field)).append(" = key.").append(getterMethodName(field))
                .append("();\n");
        }
    }

    @Override
    void appendCopyNonKeys(final StringBuilder sb, final Collection<BuilderGeneratedProperty> props) {
        for (var field : props) {
            sb.append("    this.").append(fieldName(field)).append(" = ");

            if (field.getMechanics() == ValueMechanics.NULLIFY_EMPTY) {
                sb.append(importedName(CODEHELPERS)).append(".emptyToNull(base.").append(field.getGetterName())
                    .append("());\n");
            } else {
                sb.append("base.").append(field.getGetterName()).append("();\n");
            }
        }
    }

    @Override
    void appendCopyAugmentation(final StringBuilder sb) {
        sb.append("    super(base.").append(AUGMENTATION_FIELD);
        if (keyType != null) {
            sb.append(", extractKey(base)");
        }
        sb.append(");\n");
    }
}
