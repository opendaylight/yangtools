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

        final var sc = new StringConcatenation();
        sc.append(generateDeprecatedAnnotation(targetType.getAnnotations()));
        sc.newLineIfNotEmpty();
        sc.append("private static final class ");
        sc.append(type().simpleName());
        sc.newLineIfNotEmpty();
        sc.append("    ");
        final var impIface = this.importedName(targetType);
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
        sc.append("    ");
        sc.append(generateExtractKey(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateGetters(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateNonnullGetters(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateHashCode(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateEquals(), "    ");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateToString(), "    ");
        sc.newLineIfNotEmpty();
        sc.append("}");
        sc.newLine();
        return sc;
    }

    // TODO: this is generating a utility static method for use in the (only) constructor. We should be inlining this
    //       code into the constructor once JEP-482 Flexible Constructor Bodies available. We should construct the key
    //       into a 'key' local variable, so that generateCopyKeys() below can reference it
    private String generateExtractKey() {
        if (keyType == null) {
            return "";
        }

        final var sb = new StringBuilder()
            .append('\n')
            .append("private static ").append(importedNonNull(keyType)).append(" extractKey(final ")
                .append(importedName(builder.type())).append(" base) {\n")
            .append("    final var key = base.").append(KEY_AWARE_KEY_NAME).append("();\n")
            .append("    return key != null ? key\n")
            .append("        : new ").append(importedName(keyType)).append('(');

        // Note: keys have at least one component
        final var it = keyConstructorArgs(keyType).iterator();
        while (true) {
            final var keyProp = it.next();
            sb.append("base.").append(getterMethodName(keyProp)).append("()");
            if (!it.hasNext()) {
                break;
            }
            sb.append(", ");
        }

        return sb.append(");\n").append("}\n").toString();
    }

    @Override
    public String generateDeprecatedAnnotation(final AnnotationType ann) {
        return generateAnnotation(ann);
    }

    private String generateGetters() {
        if (properties.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder();
        final var it = properties.iterator();
        while (true) {
            final var field = it.next();
            sb.append(asGetterMethod(field));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append('\n');
        }
    }

    private String generateNonnullGetters() {
        final var sb = new StringBuilder();
        var first = true;
        for (var field : properties) {
            if (field.getReturnType() instanceof GeneratedType type && GeneratorUtil.isNonPresenceContainer(type)) {
                if (first) {
                    first = false;
                } else {
                    sb.append('\n');
                }

                sb
                    .append('@').append(importedName(OVERRIDE)).append('\n')
                    .append("public ").append(importedName(type)).append(' ')
                        .append(NONNULL_PREFIX).append(StringExtensions.toFirstUpper(field.getName())).append("() {\n")
                    .append("    return ").append(importedName(JU_OBJECTS)).append(".requireNonNullElse(")
                        .append(getterMethodName(field)).append("(), ")
                        .append(type.canonicalName()).append(BUILDER_SUFFIX).append(".empty());\n")
                    .append("}\n");
            }
        }
        return sb.toString();
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

    /**
     * {@return string with the {@code hashCode()} method definition in JAVA format}
     */
    private @NonNull String generateHashCode() {
        return properties.isEmpty() && augmentType == null ? ""
            : "private int hash = 0;\n"
            + "private volatile boolean hashValid = false;\n"
            + '\n'
            + '@' + importedName(OVERRIDE) + '\n'
            + "public int hashCode() {\n"
            + "    if (hashValid) {\n"
            + "        return hash;\n"
            + "    }\n"
            + '\n'
            + "    final int result = " + importedName(targetType) + '.' + BINDING_HASHCODE_NAME + "(this);\n"
            + "    hash = result;\n"
            + "    hashValid = true;\n"
            + "    return result;\n"
            + "}\n";
    }

    /**
     * {@return string with the {@code equals()} method definition in JAVA format}
     */
    private @NonNull String generateEquals() {
        return properties.isEmpty() && augmentType == null ? ""
            : '@' + importedName(OVERRIDE) + '\n'
            + "public boolean equals(" + importedName(Types.objectType()) + " obj) {\n"
            + "    return " + importedName(targetType) + '.' + BINDING_EQUALS_NAME + "(this, obj);\n"
            + "}\n";
    }

    /**
     * {@return string with the {@code toString()} method definition in JAVA format}
     */
    private @NonNull String generateToString() {
        return '@' + importedName(OVERRIDE) + '\n'
            +  "public " + importedName(Types.STRING) + " toString() {\n"
            +  "    return " + importedName(targetType) + '.' + BINDING_TO_STRING_NAME + "(this);\n"
            +  "}\n";
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
