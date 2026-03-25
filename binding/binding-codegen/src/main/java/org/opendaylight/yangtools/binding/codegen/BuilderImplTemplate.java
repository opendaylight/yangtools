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

    private final BuilderTemplate builder;

    public BuilderImplTemplate(final BuilderTemplate builder, final GeneratedType type) {
        super(builder.javaType().getEnclosedType(type.name()), type, builder.targetType, builder.properties,
            builder.augmentType, builder.keyType);
        this.builder = builder;
    }

    @Override
    public CharSequence body() {
        StringConcatenation _builder = new StringConcatenation();
        CharSequence _generateDeprecatedAnnotation = this.generateDeprecatedAnnotation(targetType.getAnnotations());
        _builder.append(_generateDeprecatedAnnotation);
        _builder.newLineIfNotEmpty();
        _builder.append("private static final class ");
        String _simpleName = type().simpleName();
        _builder.append(_simpleName);
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        final String impIface = this.importedName(targetType);
        _builder.newLineIfNotEmpty();
        {
            if (keyType != null) {
                _builder.append("    ");
                _builder.append("extends ");
                _builder.append(importedName(ABSTRACT_ENTRY_OBJECT), "    ");
                _builder.append("<");
                _builder.append(impIface, "    ");
                _builder.append(", ");
                _builder.append(importedName(keyType), "    ");
                _builder.append(">");
                _builder.newLineIfNotEmpty();
            } else if (augmentType != null) {
                _builder.append("    ");
                _builder.append("extends ");
                _builder.append(importedName(ABSTRACT_AUGMENTABLE), "    ");
                _builder.append("<");
                _builder.append(impIface, "    ");
                _builder.append(">");
                _builder.newLineIfNotEmpty();
            }
        }
        _builder.append("    ");
        _builder.append("implements ");
        _builder.append(impIface, "    ");
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append(generateFields(true), "    ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append(generateCopyConstructor(builder.type(), type()), "    ");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append(generateExtractKey(), "    ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append(generateGetters(), "    ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append(generateNonnullGetters(), "    ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append(generateHashCode(), "    ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append(generateEquals(), "    ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append(generateToString(), "    ");
        _builder.newLineIfNotEmpty();
        _builder.append("}");
        _builder.newLine();
        return _builder;
    }

    private CharSequence generateExtractKey() {
        StringConcatenation _builder = new StringConcatenation();
        {
            if (keyType != null) {
                _builder.newLine();
                _builder.append("private static ");
                String _importedNonNull = importedNonNull(keyType);
                _builder.append(_importedNonNull);
                _builder.append(" extractKey(final ");
                String _importedName = this.importedName(builder.type());
                _builder.append(_importedName);
                _builder.append(" base) {");
                _builder.newLineIfNotEmpty();
                _builder.append("    ");
                final List<GeneratedProperty> keyProps = BaseTemplate.keyConstructorArgs(keyType);
                _builder.newLineIfNotEmpty();
                _builder.append("    ");
                _builder.append("final var key = base.");
                _builder.append(KEY_AWARE_KEY_NAME, "    ");
                _builder.append("();");
                _builder.newLineIfNotEmpty();
                _builder.append("    ");
                _builder.append("return key != null ? key");
                _builder.newLine();
                _builder.append("        ");
                _builder.append(": new ");
                String _importedName_1 = this.importedName(keyType);
                _builder.append(_importedName_1, "        ");
                _builder.append("(");
                {
                    boolean _hasElements = false;
                    for(final GeneratedProperty keyProp : keyProps) {
                        if (!_hasElements) {
                            _hasElements = true;
                        } else {
                            _builder.appendImmediate(", ", "        ");
                        }
                        _builder.append("base.");
                        String _terMethodName = BaseTemplate.getterMethodName(keyProp);
                        _builder.append(_terMethodName, "        ");
                        _builder.append("()");
                    }
                }
                _builder.append(");");
                _builder.newLineIfNotEmpty();
                _builder.append("}");
                _builder.newLine();
            }
        }
        return _builder;
    }

    @Override
    public CharSequence generateDeprecatedAnnotation(final AnnotationType ann) {
        return generateAnnotation(ann);
    }

    private CharSequence generateGetters() {
        StringConcatenation _builder = new StringConcatenation();
        {
            boolean _isEmpty = properties.isEmpty();
            boolean _not = !_isEmpty;
            if (_not) {
                {
                    boolean _hasElements = false;
                    for(final BuilderGeneratedProperty field : properties) {
                        if (!_hasElements) {
                            _hasElements = true;
                        } else {
                            _builder.appendImmediate("\n", "");
                        }
                        CharSequence _asGetterMethod = asGetterMethod(field);
                        _builder.append(_asGetterMethod);
                        _builder.newLineIfNotEmpty();
                    }
                }
            }
        }
        return _builder;
    }

    private CharSequence generateNonnullGetters() {
        StringConcatenation _builder = new StringConcatenation();
        {
            boolean _isEmpty = properties.isEmpty();
            boolean _not = !_isEmpty;
            if (_not) {
                {
                    boolean _hasElements = false;
                    for(final BuilderGeneratedProperty field : properties) {
                        if (!_hasElements) {
                            _hasElements = true;
                        } else {
                            _builder.appendImmediate("\n", "");
                        }
                        {
                            Type _returnType = field.getReturnType();
                            if (_returnType instanceof GeneratedType) {
                                {
                                    Type _returnType_1 = field.getReturnType();
                                    boolean _isNonPresenceContainer = GeneratorUtil.isNonPresenceContainer((GeneratedType) _returnType_1);
                                    if (_isNonPresenceContainer) {
                                        CharSequence _nonNullGetterMethod = nonNullGetterMethod(field);
                                        _builder.append(_nonNullGetterMethod);
                                        _builder.newLineIfNotEmpty();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return _builder;
    }

    @Override
    public CharSequence asGetterMethod(final GeneratedProperty field) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("@");
        String _importedName = this.importedName(JavaFileTemplate.OVERRIDE);
        _builder.append(_importedName);
        _builder.newLineIfNotEmpty();
        _builder.append("public ");
        String _importedName_1 = this.importedName(field.getReturnType());
        _builder.append(_importedName_1);
        _builder.append(" ");
        String _terMethodName = BaseTemplate.getterMethodName(field);
        _builder.append(_terMethodName);
        _builder.append("() {");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        final String fieldName = BaseTemplate.fieldName(field);
        _builder.newLineIfNotEmpty();
        {
            boolean _endsWith = field.getReturnType().simpleName().endsWith("[]");
            if (_endsWith) {
                _builder.append("    ");
                _builder.append("return ");
                String _importedName_2 = this.importedName(JavaFileTemplate.CODEHELPERS);
                _builder.append(_importedName_2, "    ");
                _builder.append(".copyArray(");
                _builder.append(fieldName, "    ");
                _builder.append(");");
                _builder.newLineIfNotEmpty();
            } else {
                _builder.append("    ");
                _builder.append("return ");
                _builder.append(fieldName, "    ");
                _builder.append(";");
                _builder.newLineIfNotEmpty();
            }
        }
        _builder.append("}");
        _builder.newLine();
        return _builder;
    }

    private CharSequence nonNullGetterMethod(final GeneratedProperty field) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("@");
        String _importedName = this.importedName(JavaFileTemplate.OVERRIDE);
        _builder.append(_importedName);
        _builder.newLineIfNotEmpty();
        final Type type = field.getReturnType();
        _builder.newLineIfNotEmpty();
        _builder.append("public ");
        String _importedName_1 = this.importedName(type);
        _builder.append(_importedName_1);
        _builder.append(" ");
        String _nonnullMethodName = nonnullMethodName(field);
        _builder.append(_nonnullMethodName);
        _builder.append("() {");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("return ");
        String _importedName_2 = this.importedName(JavaFileTemplate.JU_OBJECTS);
        _builder.append(_importedName_2, "    ");
        _builder.append(".requireNonNullElse(");
        String _terMethodName = BaseTemplate.getterMethodName(field);
        _builder.append(_terMethodName, "    ");
        _builder.append("(), ");
        String _canonicalName = type.canonicalName();
        _builder.append(_canonicalName, "    ");
        _builder.append(BUILDER_SUFFIX, "    ");
        _builder.append(".empty());");
        _builder.newLineIfNotEmpty();
        _builder.append("}");
        _builder.newLine();
        return _builder;
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

    // FIXME: ditch Optioanl
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
