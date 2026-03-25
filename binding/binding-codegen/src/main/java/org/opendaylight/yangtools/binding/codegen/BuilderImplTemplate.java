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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.opendaylight.yangtools.binding.lib.AbstractAugmentable;
import org.opendaylight.yangtools.binding.lib.AbstractEntryObject;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.Types;

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
        //        «targetType.annotations.generateDeprecatedAnnotation»
        //        private static final class «type.simpleName»
        //            «val impIface = targetType.importedName»
        //            «IF keyType !== null»
        //                extends «ABSTRACT_ENTRY_OBJECT.importedName»<«impIface», «keyType.importedName»>
        //            «ELSEIF augmentType !== null»
        //                extends «ABSTRACT_AUGMENTABLE.importedName»<«impIface»>
        //            «ENDIF»
        //            implements «impIface» {
        //
        //            «generateFields(true)»
        //
        //            «generateCopyConstructor(builder.type, type)»
        //            «generateExtractKey»
        //
        //            «generateGetters()»
        //
        //            «generateNonnullGetters()»
        //
        //            «generateHashCode()»
        //
        //            «generateEquals()»
        //
        //            «generateToString()»
        //        }

        final var impIface = importedName(targetType);
        final var override = importedName(OVERRIDE);

        final var sc = new StringConcatenation();
        sc.append(generateDeprecatedAnnotation(targetType.getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append("private static final class ");
        sc.append(type().simpleName());
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.newLineIfNotEmpty();
        if (keyType != null) {
            sc.append("    ");
            sc.append("extends ");
            sc.append(importedName(ABSTRACT_ENTRY_OBJECT), "    ");
            sc.append("<");
            sc.append(impIface, "    ");
            sc.append(", ");
            sc.append(importedName(keyType), "    ");
            sc.append(">");
            sc.newLineIfNotEmpty();
        } else if (augmentType != null) {
            sc.append("    ");
            sc.append("extends ");
            sc.append(importedName(ABSTRACT_AUGMENTABLE), "    ");
            sc.append("<");
            sc.append(impIface, "    ");
            sc.append(">");
            sc.newLineIfNotEmpty();
        }
        sc.append("    ");
        sc.append("implements ");
        sc.append(impIface, "    ");
        sc.append(" {\n");

        // generate instance fields
        if (!properties.isEmpty()) {
            sc.newLine();
            for (var prop : properties) {
                sc.append("    private final ");
                sc.append(importedName(prop.getReturnType()));
                sc.append(" ");
                sc.append(fieldName(prop));
                sc.append(";\n");
            }
        }

        sc.newLine();
        sc.append("    ");
        sc.append(generateCopyConstructor(builder.type(), type()), "    ");
        sc.newLineIfNotEmpty();

        if (keyType != null) {
            // TODO: this is generating a utility static method for use in the (only) constructor. We should be inlining
            //       this code into the constructor once JEP-482 Flexible Constructor Bodies available. We should
            //       construct the key into a 'key' local variable, so that generateCopyKeys() below can reference it
            sc.newLine();
            sc.append("    private static ");
            sc.append(importedNonNull(keyType));
            sc.append(" extractKey(final ");
            sc.append(importedName(builder.type()));
            sc.append(" base) {\n");
            sc.append("        final var key = base.");
            sc.append(KEY_AWARE_KEY_NAME);
            sc.append("();\n");
            sc.append("        return key != null ? key\n");
            sc.append("            : new ");
            sc.append(importedName(keyType));
            sc.append("(");

            // Note: keys have at least one component
            final var it = keyConstructorArgs(keyType).iterator();
            while (true) {
                final var keyProp = it.next();
                sc.append("base.");
                sc.append(getterMethodName(keyProp));
                sc.append("()");
                if (!it.hasNext()) {
                    break;
                }
                sc.append(", ");
            }

            sc.append(");\n");
            sc.append("    }\n");
        }

        // generate normal getters
        if (!properties.isEmpty()) {
            sc.newLine();

            final var it = properties.iterator();
            while (true) {
                final var field = it.next();
                sc.append("    ");
                sc.append(asGetterMethod(field), "    ");

                if (field.getReturnType() instanceof GeneratedType type && GeneratorUtil.isNonPresenceContainer(type)) {
                    sc.newLine();
                    sc.append("    @");
                    sc.append(override);
                    sc.newLine();
                    sc.append("    public ");
                    sc.append(importedName(type));
                    sc.append(" ");
                    sc.append(NONNULL_PREFIX);
                    sc.append(StringExtensions.toFirstUpper(field.getName()));
                    sc.append("() {\n");
                    sc.append("        return ");
                    sc.append(importedName(JU_OBJECTS));
                    sc.append(".requireNonNullElse(");
                    sc.append(getterMethodName(field));
                    sc.append("(), ");
                    sc.append(type.canonicalName());
                    sc.append(BUILDER_SUFFIX);
                    sc.append(".empty());\n");
                    sc.append("}\n");
                }

                if (!it.hasNext()) {
                    break;
                }
                sc.newLine();
            }
        }

        // generate hashCode/equals if needed
        if (!properties.isEmpty() || augmentType != null) {
            sc.newLine();
            sc.append("    private int hash = 0;\n");
            sc.append("    private volatile boolean hashValid = false;\n");
            sc.newLine();
            sc.append("    @");
            sc.append(override);
            sc.newLine();
            sc.append("    public int hashCode() {\n");
            sc.append("        if (hashValid) {\n");
            sc.append("            return hash;\n");
            sc.append("        }\n");
            sc.newLine();
            sc.append("        final int result = ");
            sc.append(impIface);
            sc.append(".");
            sc.append(BINDING_HASHCODE_NAME);
            sc.append("(this);\n");
            sc.append("        hash = result;\n");
            sc.append("        hashValid = true;\n");
            sc.append("        return result;\n");
            sc.append("    }\n");
            sc.newLine();
            sc.append("    @");
            sc.append(override);
            sc.newLine();
            sc.append("    public boolean equals(");
            sc.append(importedName(Types.objectType()));
            sc.append(" obj) {\n");
            sc.append("        return ");
            sc.append(impIface);
            sc.append(".");
            sc.append(BINDING_EQUALS_NAME);
            sc.append("(this, obj);\n");
            sc.append("    }\n");
        }

        // generate equals()
        sc.newLine();
        sc.append("    @");
        sc.append(override);
        sc.newLine();
        sc.append("    public ");
        sc.append(importedName(Types.STRING));
        sc.append(" toString() {\n");
        sc.append("        return ");
        sc.append(impIface);
        sc.append(".");
        sc.append(BINDING_TO_STRING_NAME);
        sc.append("(this);\n");
        sc.append("    }\n");

        sc.append("}\n");
        return sc;
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

    MethodSignature findGetter(final String getterName) {
        final var type = type();
        final var ownGetter = getterByName(nonDefaultMethods(type), getterName);
        if (ownGetter.isPresent()) {
            return ownGetter.orElseThrow();

        }
        for (var ifc : type.getImplements()) {
            if (ifc instanceof GeneratedType genInterface) {
                final var getter = findGetter(genInterface, getterName);
                if (getter.isPresent()) {
                    return getter.orElseThrow();
                }
            }
        }

        throw new IllegalStateException(
            "%s should be present in %s type or in one of its ancestors as getter".formatted(
                propertyNameFromGetter(getterName), type));
    }

    // FIXME: ditch Optional and merge with the above method
    private static Optional<MethodSignature> findGetter(final GeneratedType implType, final String getterName) {
        final var getter = getterByName(nonDefaultMethods(implType), getterName);
        if (getter.isPresent()) {
            return getter;
        }
        for (var ifc : implType.getImplements()) {
            if (ifc instanceof GeneratedType genInterface) {
                final var getterImpl = findGetter(genInterface, getterName);
                if (getterImpl.isPresent()) {
                    return getterImpl;
                }
            }
        }

        return Optional.empty();
    }

    @Override
    String generateCopyKeys(final List<GeneratedProperty> keyProps) {
        final var sb = new StringBuilder().append("final var key = key();\n");
        for (var field : keyProps) {
            sb.append("this.").append(fieldName(field)).append(" = key.").append(getterMethodName(field))
                .append("();\n");
        }
        return sb.toString();
    }

    @Override
    String generateCopyNonKeys(final Collection<BuilderGeneratedProperty> props) {
        final var sb = new StringBuilder();
        for (var field : props) {
            sb.append("this.").append(fieldName(field)).append(" = ");

            if (field.getMechanics() == ValueMechanics.NULLIFY_EMPTY) {
                sb.append(importedName(CODEHELPERS)).append(".emptyToNull(base.").append(field.getGetterName())
                    .append("());\n");
            } else {
                sb.append("base.").append(field.getGetterName()).append("();\n");
            }
        }
        return sb.toString();
    }

    @Override
    String generateCopyAugmentation(final Type implType) {
        return keyType == null ? "super(base." + AUGMENTATION_FIELD + ");\n"
            : "super(base." + AUGMENTATION_FIELD + ", extractKey(base));\n";
    }
}
