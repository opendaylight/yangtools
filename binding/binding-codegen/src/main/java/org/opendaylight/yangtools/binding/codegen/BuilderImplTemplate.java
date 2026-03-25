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
import org.eclipse.jdt.annotation.NonNullByDefault;
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
     * {@code AbstractAugmentable} as a JavaTypeName.
     */
    private static final @NonNull JavaTypeName ABSTRACT_AUGMENTABLE = JavaTypeName.create(AbstractAugmentable.class);

    /**
     * {@code AbstractAugmentable} as a JavaTypeName.
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
        sc.append(" {");
        sc.newLineIfNotEmpty();
        sc.newLine();
        sc.append("    ");
        sc.append(generateFields(true), "    ");
        sc.newLineIfNotEmpty();
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
    private CharSequence generateExtractKey() {
//        «IF keyType !== null»
//
//            private static «keyType.importedNonNull» extractKey(final «builder.type.importedName» base) {
//                «val keyProps = keyConstructorArgs(keyType)»
//                final var key = base.«KEY_AWARE_KEY_NAME»();
//                return key != null ? key
//                    : new «keyType.importedName»(«FOR keyProp : keyProps SEPARATOR ", "»base.«keyProp.getterMethodName»()«ENDFOR»);
//            }
//        «ENDIF»

        if (keyType == null) {
            return "";
        }

        final var sc = new StringConcatenation();
        sc.newLine();
        sc.append("private static ");
        sc.append(importedNonNull(keyType));
        sc.append(" extractKey(final ");
        sc.append(importedName(builder.type()));
        sc.append(" base) {");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        final var keyProps = keyConstructorArgs(keyType);
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append("final var key = base.");
        sc.append(KEY_AWARE_KEY_NAME, "    ");
        sc.append("();");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append("return key != null ? key");
        sc.newLine();
        sc.append("        ");
        sc.append(": new ");
        sc.append(importedName(keyType), "        ");
        sc.append("(");

        boolean _hasElements = false;
        for (var keyProp : keyProps) {
            if (!_hasElements) {
                _hasElements = true;
            } else {
                sc.appendImmediate(", ", "        ");
            }
            sc.append("base.");
            sc.append(getterMethodName(keyProp), "        ");
            sc.append("()");
        }

        sc.append(");");
        sc.newLineIfNotEmpty();
        sc.append("}");
        sc.newLine();
        return sc;
    }

    @Override
    public String generateDeprecatedAnnotation(final AnnotationType ann) {
        return generateAnnotation(ann);
    }

    private CharSequence generateGetters() {
        //    «IF !properties.empty»
        //        «FOR field : properties SEPARATOR '\n'»
        //            «field.asGetterMethod»
        //        «ENDFOR»
        //    «ENDIF»
        if (properties.isEmpty()) {
            return "";
        }

        final var sc = new StringConcatenation();
        boolean _hasElements = false;
        for (var field : properties) {
            if (!_hasElements) {
                _hasElements = true;
            } else {
                sc.appendImmediate("\n", "");
            }
            sc.append(asGetterMethod(field));
            sc.newLineIfNotEmpty();
        }
        return sc;
    }

    private CharSequence generateNonnullGetters() {
        if (properties.isEmpty()) {
            return "";
        }

        //        «FOR field : properties SEPARATOR '\n'»
        //            «IF field.returnType instanceof GeneratedType»
        //                «IF isNonPresenceContainer(field.returnType as GeneratedType)»
        //                    «field.nonNullGetterMethod»
        //                «ENDIF»
        //            «ENDIF»
        //        «ENDFOR»

        final var sc = new StringConcatenation();
        boolean _hasElements = false;
        for (var field : properties) {
           if (!_hasElements) {
                _hasElements = true;
            } else {
                sc.appendImmediate("\n", "");
            }
            if (field.getReturnType() instanceof GeneratedType returnType) {
                if (GeneratorUtil.isNonPresenceContainer(returnType)) {
                    sc.append(nonNullGetterMethod(field));
                    sc.newLineIfNotEmpty();
                }
            }
        }
        return sc;
    }

    @Override
    public CharSequence asGetterMethod(final GeneratedProperty field) {
//        @«OVERRIDE.importedName»
//        public «field.returnType.importedName» «field.getterMethodName»() {
//            «val fieldName = field.fieldName»
//            «IF field.returnType.simpleName.endsWith("[]")»
//                return «CODEHELPERS.importedName».copyArray(«fieldName»);
//            «ELSE»
//                return «fieldName»;
//            «ENDIF»
//        }

        final var sc = new StringConcatenation();
        sc.append("@");
        sc.append(importedName(OVERRIDE));
        sc.newLineIfNotEmpty();
        sc.append("public ");
        sc.append(importedName(field.getReturnType()));
        sc.append(" ");
        sc.append(getterMethodName(field));
        sc.append("() {");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        final String fieldName = BaseTemplate.fieldName(field);
        sc.newLineIfNotEmpty();
        if (field.getReturnType().simpleName().endsWith("[]")) {
            sc.append("    ");
            sc.append("return ");
            sc.append(importedName(CODEHELPERS), "    ");
            sc.append(".copyArray(");
            sc.append(fieldName, "    ");
            sc.append(");");
            sc.newLineIfNotEmpty();
        } else {
            sc.append("    ");
            sc.append("return ");
            sc.append(fieldName, "    ");
            sc.append(";");
            sc.newLineIfNotEmpty();
        }
        sc.append("}");
        sc.newLine();
        return sc;
    }

    private CharSequence nonNullGetterMethod(final GeneratedProperty field) {
//        @«OVERRIDE.importedName»
//        «val type = field.returnType»
//        public «type.importedName» «field.nonnullMethodName»() {
//            return «JU_OBJECTS.importedName».requireNonNullElse(«field.getterMethodName»(), «type.canonicalName»«BUILDER_SUFFIX».empty());
//        }

        final var type = field.getReturnType();
        final var sc = new StringConcatenation();
        sc.append("@");
        sc.append(importedName(OVERRIDE));
        sc.newLineIfNotEmpty();
        sc.newLineIfNotEmpty();
        sc.append("public ");
        sc.append(importedName(type));
        sc.append(" ");
        sc.append(nonnullMethodName(field));
        sc.append("() {");
        sc.newLineIfNotEmpty();
        sc.append("    ");
        sc.append("return ");
        sc.append(importedName(JU_OBJECTS), "    ");
        sc.append(".requireNonNullElse(");
        sc.append(getterMethodName(field), "    ");
        sc.append("(), ");
        sc.append(type.canonicalName(), "    ");
        sc.append(BUILDER_SUFFIX, "    ");
        sc.append(".empty());");
        sc.newLineIfNotEmpty();
        sc.append("}");
        sc.newLine();
        return sc;
    }

    @NonNullByDefault
    private static String nonnullMethodName(final GeneratedProperty field) {
        return NONNULL_PREFIX + StringExtensions.toFirstUpper(field.getName());
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
     * Template method which generates the method <code>hashCode()</code>.
     *
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    private CharSequence generateHashCode() {
//        «IF !properties.empty || augmentType !== null»
//            private int hash = 0;
//            private volatile boolean hashValid = false;
//
//            @«OVERRIDE.importedName»
//            public int hashCode() {
//                if (hashValid) {
//                    return hash;
//                }
//
//                final int result = «targetType.importedName».«BINDING_HASHCODE_NAME»(this);
//                hash = result;
//                hashValid = true;
//                return result;
//            }
//        «ENDIF»

        final var sc = new StringConcatenation();
        if (!properties.isEmpty() || augmentType != null) {
            sc.append("private int hash = 0;");
            sc.newLine();
            sc.append("private volatile boolean hashValid = false;");
            sc.newLine();
            sc.newLine();
            sc.append("@");
            sc.append(importedName(OVERRIDE));
            sc.newLineIfNotEmpty();
            sc.append("public int hashCode() {");
            sc.newLine();
            sc.append("    ");
            sc.append("if (hashValid) {");
            sc.newLine();
            sc.append("        ");
            sc.append("return hash;");
            sc.newLine();
            sc.append("    ");
            sc.append("}");
            sc.newLine();
            sc.newLine();
            sc.append("    ");
            sc.append("final int result = ");
            sc.append(importedName(targetType), "    ");
            sc.append(".");
            sc.append(BINDING_HASHCODE_NAME, "    ");
            sc.append("(this);");
            sc.newLineIfNotEmpty();
            sc.append("    ");
            sc.append("hash = result;");
            sc.newLine();
            sc.append("    ");
            sc.append("hashValid = true;");
            sc.newLine();
            sc.append("    ");
            sc.append("return result;");
            sc.newLine();
            sc.append("}");
            sc.newLine();
        }
        return sc;
    }

    /**
     * {@return string with the {@code equals()} method definition in JAVA format}
     */
    private String generateEquals() {
       return properties.isEmpty() && augmentType == null ? "" : new StringBuilder()
            .append('@').append(importedName(OVERRIDE)).append('\n')
            .append("public boolean equals(").append(importedName(Types.objectType())).append(" obj) {\n")
            .append("    return ").append(importedName(targetType)).append('.').append(BINDING_EQUALS_NAME)
                .append("(this, obj);\n")
            .append("}\n")
            .toString();
    }

    /**
     * {@return string with the {@code toString()} method definition in JAVA format}
     */
    private String generateToString() {
        return new StringBuilder()
            .append('@').append(importedName(OVERRIDE)).append('\n')
            .append("public ").append(importedName(Types.STRING)).append(" toString() {\n")
            .append("    return ").append(importedName(targetType)).append('.').append(BINDING_TO_STRING_NAME)
                .append("(this);\n")
            .append("}\n")
            .toString();
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
        final var sb = new StringBuilder().append("super(base.").append(AUGMENTATION_FIELD);
        if (keyType != null) {
            sb.append(", extractKey(base)");
        }
        return sb.append(");").toString();
    }
}
