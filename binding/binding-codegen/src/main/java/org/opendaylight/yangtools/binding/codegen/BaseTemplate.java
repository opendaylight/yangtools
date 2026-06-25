/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.QNAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.YANGDATANAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.nameInModuleOf;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.yangModuleInfoOf;
import static org.opendaylight.yangtools.binding.contract.Naming.BUILDER_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.GETTER_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.NAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstUpper;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.extractAugmentationTarget;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.isNotificationBody;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;

import com.google.common.base.VerifyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter;

abstract sealed class BaseTemplate extends JavaFileTemplate
        permits ArchetypeTemplate, BuilderImplTemplate, BuilderTemplate {
    static final Comparator<GeneratedProperty> PROP_COMPARATOR = Comparator.comparing(GeneratedProperty::getName);

    /**
     * Name or prefix (multiple patterns in builder class as composed with '_' and upper case of the field name)
     * of the class constant which contains list of <code>Pattern</code> instances. The type of this constant is
     * Pattern[] for more than one pattern, or Pattern if there is only a single one.
     */
    static final String MEMBER_PATTERN_LIST = "patterns";

    /**
     * Name or prefix (multiple patterns in builder class as composed with '_' and upper case of the field name)
     * of the class constant which contains a list of XSD regular expression strings. The type of this constant is
     * String[] (or String for single strings) and it corresponds to {@link #MEMBER_PATTERN_LIST} in both size
     * and ordering.
     */
    static final String MEMBER_REGEX_LIST = "regexes";

    private static final DeclaredStatementFormatter YANG_FORMATTER = DeclaredStatementFormatter.builder()
        .addIgnoredStatement(ContactStatement.DEF)
        .addIgnoredStatement(DescriptionStatement.DEF)
        .addIgnoredStatement(OrganizationStatement.DEF)
        .addIgnoredStatement(ReferenceStatement.DEF)
        .build();

    private final @NonNull Archetype archetype;

    @NonNullByDefault
    BaseTemplate(final GeneratedClass javaType, final Archetype archetype) {
        super(javaType);
        this.archetype = requireNonNull(archetype);
    }

    /**
     * {@return the type this template is bound to}
     */
    final @NonNull Archetype type() {
        return archetype;
    }

    @Override
    final void generateTo(final Appendable out) throws IOException {
        // FIXME: of this code should live in GeneratedClass.of() or thereabout
        final var javaType = javaType();
        if (!(javaType instanceof GeneratedClass.TopLevel topLevel)) {
            throw new VerifyException("Unexpected type " + javaType);
        }

        // note: this has a side-effect of populating imports
        final var body = body().build();

        // package declaration
        out.append("package ").append(javaType.name().packageName()).append(";\n\n");

        // import block
        final var importedNames = topLevel.imports().toArray(JavaTypeName[]::new);
        for (var importedName : importedNames) {
            out.append("import ").append(importedName.canonicalName()).append(";\n");
        }
        if (importedNames.length != 0) {
            out.append('\n');
        }

        // body
        body.appendTo(out);
    }

    /**
     * {@return Body of this Java file}
     */
    abstract @NonNull BlockBuilder body();

    // Helper patterns
    static final @NonNull String fieldName(final GeneratedProperty property) {
        return "_" + property.getName();
    }

    @NonNullByDefault
    final String emitConstant(final Constant constant) {
        final var name = constant.getName();
        final var type = constant.getType();

        return switch (name) {
            case NAME_STATIC_FIELD_NAME -> emitNameConstant(name, type, (YangDataName) constant.getValue());
            case QNAME_STATIC_FIELD_NAME -> emitQNameConstant(name, type, (String) constant.getValue());
            default -> "public static final " + importedName(type) + ' ' + name + " = " + constant.getValue() + ";\n";
        };
    }

    @NonNullByDefault
    private String emitNameConstant(final String name, final Type type, final YangDataName yangDataName) {
        final var yangModuleInfo = yangModuleInfoOf(yangDataName.module());
        return """
            /**
             * Yang Data template name of the statement represented by this class.
             */
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo)
                + '.' + YANGDATANAMEOF_METHOD_NAME + "(\"" + yangDataName.name() + "\");\n";
    }

    @NonNullByDefault
    final String emitQNameConstant(final String name, final Type type, final String localName) {
        final var yangModuleInfo = nameInModuleOf(archetype);
        return """
            /**
             * YANG identifier of the statement represented by this class.
             */
            public static final\s""" + importedNonNull(type) + ' ' + name + " = " + importedName(yangModuleInfo)
                + '.' + QNAMEOF_METHOD_NAME + "(\"" + localName + "\");\n";
    }

    /**
     * Template method which generates the getter method for {@code field}.
     *
     * @param field generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format
     */
    // FIXME: return a Block when we can do efficient copies
    @NonNullByDefault
    final BlockBuilder asGetterMethod(final GeneratedProperty field) {
        return newBlockBuilder()
            .str("public ").str(importedReturnType(field)).sp().str(getterMethodName(field)).str("()").jBlock(bb -> {
                final var fieldName = fieldName(field);
                bb.str("return ");
                // any Java array type needs to be duplicated to prevent modification
                if (field.getReturnType().isArray()) {
                    bb.str(importedName(CODEHELPERS)).str(".copyArray(").str(fieldName).eol(");");
                } else {
                    bb.str(fieldName).eS();
                }
            }).nl();
    }

    @NonNullByDefault
    final void appendSnippet(final StringBuilder sb, final Archetype type, final ModuleEffectiveStatement module,
            final EffectiveStatement<?, ?> stmt, final DocumentedNode node) {
        appendYangSnippet(sb
            .append('\n')
            .append("<p>\n")
            .append("This class represents the following YANG schema fragment defined in module <b>")
            .append(module.argument().getLocalName()).append("</b>\n")
            .append("<pre>\n"),
            module, stmt.requireDeclared())
            .append("</pre>");

        if (node instanceof SchemaNode schema) {
            // sb.append("The schema path to identify an instance is\n");
            // appendPath(sb.append("<i>"), def.getModule(), schema.getPath().getPathFromRoot());
            // sb.append("</i>\n");

            if (schema instanceof ContainerSchemaNode || schema instanceof ListSchemaNode
                || schema instanceof NotificationDefinition && !isNotificationBody(type)) {
                final var simpleName = type.simpleName();
                final var builderName = simpleName + BUILDER_SUFFIX;

                sb
                    .append("\n<p>To create instances of this class use {@link ").append(builderName)
                    .append("}.\n")
                    .append("@see ").append(builderName).append('\n');
                if (node instanceof ListSchemaNode list) {
                    final var keyDef = list.getKeyDefinition();
                    if (!keyDef.isEmpty()) {
                        sb.append("@see ").append(simpleName).append(KEY_SUFFIX);
                    }
                    sb.append('\n');
                }
            }
        } else if (stmt instanceof AugmentEffectiveStatement) {
            // Find target Augmentation<Foo> and reference Foo
            final var augType = findAugmentationArgument(type);
            if (augType != null) {
                sb
                    .append("\n\n")
                    .append("@see ").append(importedName(augType));
            }
        }
        // FIXME: this is equivalent to genTo.isTypedef() so we should be able to unify the two concepts -- but really
        //        that soulds like it should be handled in those templates ... perhaps we should receive these from
        //        the caller as 'List<JavaTypeName> seeAlso'?
        if (stmt instanceof TypedefEffectiveStatement && type instanceof GeneratedTransferObject<?> genTO) {
            final var augType = genTO.getSuperType();
            if (augType != null) {
                sb
                    .append("\n\n")
                    .append("@see ").append(augType.simpleName());
            }
        }
    }

    // FIXME: return BlockFragment
    @NonNullByDefault
    private static StringBuilder appendYangSnippet(final StringBuilder sb, final ModuleEffectiveStatement module,
            final DeclaredStatement<?> stmt) {
        for (var str : YANG_FORMATTER.toYangTextSnippet(module, stmt)) {
            sb.append(DocUtils.replaceAllIllegalChars(DocUtils.encodeAngleBrackets(encodeJavadocSymbols(str))));
        }
        return sb;
    }

    private static @Nullable Type findAugmentationArgument(final Archetype genType) {
        if (genType instanceof LegacyArchetype archetype) {
            for (var implType : archetype.getImplements()) {
                if (implType instanceof ParameterizedType parameterized) {
                    final var augmentType = extractAugmentationTarget(parameterized);
                    if (augmentType != null) {
                        return augmentType;
                    }
                }
            }
        }
        return null;
    }

    @NonNullByDefault
    final BlockBuilder generateAnnotation(final AnnotationType annotation) {
        final var bb = newBlockBuilder()
            .at().str(importedName(annotation));

        final var params = annotation.getParameters();
        if (params != null && !params.isEmpty()) {
            bb.eol("(");

            final var it = params.iterator();
            while (true) {
                final var param = it.next();
                bb.str("    ").str(param.getName()).str("=").str(param.getValue());
                if (!it.hasNext()) {
                    break;
                }
                bb.eol(",");
            }

            bb.nl().str(")");
        }

        return bb.nl();
    }

    @NonNullByDefault
    final BlockBuilder generateCheckers(final GeneratedProperty field, final Restrictions restrictions,
            final Type actualType) {
        verify(!restrictions.isEmpty());

        final var bb = newBlockBuilder();
        restrictions.getRangeConstraint().ifPresent(range ->
            bb.blk(AbstractRangeGenerator.forType(actualType).generateRangeChecker(
                    toFirstUpper(field.getName()), range, javaType())));
        // FIXME: this call looks unlike the range checker call: it should be refactored to acquire a generator,
        //        so that we can suppress checker when not needed -- just like ranges do above
        restrictions.getLengthConstraint().ifPresent(length ->
            bb.blk(LengthGenerator.generateLengthChecker(fieldName(field), actualType, length, javaType()))
        );
        return bb;
    }

    // FIXME: remove this concatenation
    static final @NonNull String getterMethodName(final @NonNull String propName) {
        return GETTER_PREFIX + toFirstUpper(propName);
    }

    static final @NonNull String getterMethodName(final GeneratedProperty field) {
        return getterMethodName(field.getName());
    }

    static final @Nullable BlockBuilder wrapToDocumentation(final @NonNull String text) {
        // TODO: isBlank()?
        if (text.isEmpty()) {
            return null;
        }

        final var bb = Block.builder();
        appendAsJavadoc(bb, text);
        return bb;
    }

    @NonNullByDefault
    static final void appendAsJavadoc(final BlockBuilder bb, final String text) {
        bb.eol("/**");

        final int length = text.length();
        int begin = 0;
        while (begin < length) {
            bb.str(" *");

            final int nl = text.indexOf('\n', begin);
            final int end = nl != -1 ? nl : length;
            appendLine(bb, text, begin, end);
            begin = end + 1;
        }

        bb.eol(" */");
    }

    @NonNullByDefault
    private static void appendLine(final BlockBuilder bb, final String str, final int start, final int limit) {
        // do not emit obvious trailing whitespace
        int end = limit;
        while (true) {
            if (end == start) {
                bb.newLine();
                return;
            }

            final int prev = end - 1;
            final char ch = str.charAt(prev);
            if (ch != ' ' && ch != '\t') {
                break;
            }

            end = prev;
        }

        bb.sp().eol(str, start, end);
    }

    // FIXME: return a Block
    @NonNullByDefault
    final BlockBuilder checkFieldValue(final LegacyArchetype type, final GeneratedProperty property,
            final Restrictions restrictions, final Type actualType, final String value) {
        verify(!restrictions.isEmpty());

        final var valueRef = actualType instanceof ConcreteType ? value : value + ".getValue()";

        final var bb = newBlockBuilder();
        if (restrictions.getRangeConstraint().isPresent()) {
            AbstractRangeGenerator.forType(actualType)
                .appendCheckerCall(bb, toFirstUpper(property.getName()), valueRef);
        }

        final var fieldName = fieldName(property);
        if (restrictions.getLengthConstraint().isPresent()) {
            LengthGenerator.appendCheckerCall(bb, fieldName, valueRef);
        }

        final var fieldUpperCase = fieldName.toUpperCase(Locale.ROOT);

        for (var currentConstant : type.getConstantDefinitions()) {
            final var currentName = currentConstant.getName();

            if (currentName.startsWith(PATTERN_CONSTANT_NAME)
                && fieldUpperCase.equals(currentName.substring(PATTERN_CONSTANT_NAME.length()))) {
                bb.str(importedName(CODEHELPERS)).str(".checkPattern(value, " + MEMBER_PATTERN_LIST)
                    .str(fieldName).str(", " + MEMBER_REGEX_LIST).str(fieldName).eol(");");
            }
        }

        return bb;
    }

    final @Nullable BlockBuilder generateInnerClasses(final @NonNull DataRootArchetype root,
            final List<Archetype> innerTypes) {
        final var innerClasses = new ArrayList<BlockBuilder>();
        for (var innerType : innerTypes) {
            if (innerType instanceof TypeObjectArchetype<?> gto) {
                final var innerJavaType = javaType().getNestedClass(gto);
                innerClasses.add(switch (gto) {
                    case BitsTypeObjectArchetype bitsTO ->
                        BitsTypeObjectTemplate.generateInner(innerJavaType, bitsTO, root);
                    case EnumTypeObjectArchetype enumTO ->
                        EnumTypeObjectTemplate.generateInner(innerJavaType, enumTO, root);
                    case ScalarTypeObjectArchetype scalarTO ->
                        ScalarTypeObjectTemplate.generateInner(innerJavaType, scalarTO, root);
                    case UnionTypeObjectArchetype unionTO ->
                        UnionTypeObjectTemplate.generateInner(innerJavaType, unionTO, root);
                });
            }
        }
        if (innerClasses.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        final var it = innerClasses.iterator();
        while (true) {
            bb.blk(it.next());
            if (!it.hasNext()) {
                break;
            }
            bb.newLine();
        }
        return bb;
    }
}
