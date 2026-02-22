/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for {@link EnumTypeObject}s.
 */
@NonNullByDefault
final class EnumTypeObjectTemplate extends BaseTemplate {
    private static final JavaTypeName ENUM_TYPE_OBJECT = JavaTypeName.create(EnumTypeObject.class);

    private final EnumTypeObjectArchetype archetype;

    EnumTypeObjectTemplate(final AbstractJavaGeneratedType javaType, final EnumTypeObjectArchetype archetype) {
        super(javaType, archetype);
        this.archetype = archetype;
    }

    EnumTypeObjectTemplate(final EnumTypeObjectArchetype archetype) {
        super(archetype);
        this.archetype = archetype;
    }

    CharSequence generateAsInnerClass() {
        return body();
    }

    @Override
    CharSequence body() {
        // calculate imports up front
        final var codeHelpers = importedName(CODEHELPERS);
        final var enumTypeObject = importedName(ENUM_TYPE_OBJECT);
        final var iae = importedName(IAE);
        final var nonnullSelf = importedNonNull(archetype);
        final var nonnullString = importedNonNull(Types.STRING);
        final var npe = importedName(NPE);
        final var nullableSelf = importedNullable(archetype);
        final var override = importedName(OVERRIDE);
        final var string = importedName(Types.STRING);

        //        «enums.formatDataForJavaDoc.wrapToDocumentation»
        //        «generatedAnnotation»
        //        public enum «enums.simpleName» implements «ENUM_TYPE_OBJECT.importedName» {
        //            «writeEnumeration(enums)»
        //
        //            private final «STRING.importedNonNull» name;
        //            private final int value;
        //
        //            private «enums.simpleName»(int value, «STRING.importedNonNull» name) {
        //                this.value = value;
        //                this.name = name;
        //            }
        //
        //            @«OVERRIDE.importedName»
        //            public «STRING.importedNonNull» getName() {
        //                return name;
        //            }
        //
        //            @«OVERRIDE.importedName»
        //            public int getIntValue() {
        //                return value;
        //            }
        //
        //            /**
        //             * Return the enumeration member whose {@link #getName()} matches specified assigned name.
        //             *
        //             * @param name YANG assigned name
        //             * @return corresponding «enums.simpleName» item, or {@code null} if no such item exists
        //             * @throws «NPE.importedName» if {@code name} is null
        //             */
        //            public static «enums.importedNullable» forName(«STRING.importedName» name) {
        //                return switch (name) {
        //                    «FOR v : enums.values»
        //                    case "«v.name»" -> «v.constantName»;
        //                    «ENDFOR»
        //                    default -> null;
        //                };
        //            }
        //
        //            /**
        //             * Return the enumeration member whose {@link #getIntValue()} matches specified value.
        //             *
        //             * @param intValue integer value
        //             * @return corresponding «enums.simpleName» item, or {@code null} if no such item exists
        //             */
        //            public static «enums.importedNullable» forValue(int intValue) {
        //                return switch (intValue) {
        //                    «FOR v : enums.values»
        //                    case «v.value» -> «v.constantName»;
        //                    «ENDFOR»
        //                    default -> null;
        //                };
        //            }
        //
        //            /**
        //             * Return the enumeration member whose {@link #getName()} matches specified assigned name.
        //             *
        //             * @param name YANG assigned name
        //             * @return corresponding «enums.simpleName» item
        //             * @throws «NPE.importedName» if {@code name} is null
        //             * @throws «IAE.importedName» if {@code name} does not match any item
        //             */
        //            public static «enums.importedNonNull» ofName(«STRING.importedName» name) {
        //                return «CODEHELPERS.importedName».checkEnum(forName(name), name);
        //            }
        //
        //            /**
        //             * Return the enumeration member whose {@link #getIntValue()} matches specified value.
        //             *
        //             * @param intValue integer value
        //             * @return corresponding «enums.simpleName» item
        //             * @throws «IAE.importedName» if {@code intValue} does not match any item
        //             */
        //            public static «enums.importedNonNull» ofValue(int intValue) {
        //                return «CODEHELPERS.importedName».checkEnum(forValue(intValue), intValue);
        //            }
        //        }

        final var _builder = new StringConcatenation();
        _builder.append(wrapToDocumentation(formatDataForJavaDoc(archetype)));
        _builder.newLineIfNotEmpty();
        _builder.append(generatedAnnotation());
        _builder.newLineIfNotEmpty();
        _builder.append("public enum ");
        _builder.append(archetype.simpleName());
        _builder.append(" implements ");
        _builder.append(enumTypeObject);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append(writeValues(), "    ");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("private final ");
        _builder.append(nonnullString, "    ");
        _builder.append(" name;");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("private final int value;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("private ");
        _builder.append(archetype.simpleName(), "    ");
        _builder.append("(int value, ");
        _builder.append(nonnullString, "    ");
        _builder.append(" name) {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("this.value = value;");
        _builder.newLine();
        _builder.append("        ");
        _builder.append("this.name = name;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("@");
        _builder.append(override, "    ");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("public ");
        _builder.append(nonnullString, "    ");
        _builder.append(" getName() {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("return name;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("@");
        _builder.append(override, "    ");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("public int getIntValue() {");
        _builder.newLine();
        _builder.append("        ");
        _builder.append("return value;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("/**");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* Return the enumeration member whose {@link #getName()} matches specified assigned name.");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("*");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @param name YANG assigned name");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @return corresponding ");
        _builder.append(archetype.simpleName(), "     ");
        _builder.append(" item, or {@code null} if no such item exists");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("* @throws ");
        _builder.append(npe, "     ");
        _builder.append(" if {@code name} is null");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("public static ");
        _builder.append(nullableSelf, "    ");
        _builder.append(" forName(");
        _builder.append(string, "    ");
        _builder.append(" name) {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("return switch (name) {");
        _builder.newLine();
        for (var value : archetype.values()) {
            _builder.append("            ");
            _builder.append("case \"");
            _builder.append(value.name(), "            ");
            _builder.append("\" -> ");
            _builder.append(value.constantName(), "            ");
            _builder.append(";");
            _builder.newLineIfNotEmpty();
        }
        _builder.append("            ");
        _builder.append("default -> null;");
        _builder.newLine();
        _builder.append("        ");
        _builder.append("};");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("/**");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* Return the enumeration member whose {@link #getIntValue()} matches specified value.");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("*");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @param intValue integer value");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @return corresponding ");
        _builder.append(archetype.simpleName(), "     ");
        _builder.append(" item, or {@code null} if no such item exists");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("public static ");
        _builder.append(nullableSelf, "    ");
        _builder.append(" forValue(int intValue) {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("return switch (intValue) {");
        _builder.newLine();
        for (var value : archetype.values()) {
            _builder.append("            ");
            _builder.append("case ");
            _builder.append(value.value(), "            ");
            _builder.append(" -> ");
            _builder.append(value.constantName(), "            ");
            _builder.append(";");
            _builder.newLineIfNotEmpty();
        }
        _builder.append("            ");
        _builder.append("default -> null;");
        _builder.newLine();
        _builder.append("        ");
        _builder.append("};");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("/**");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* Return the enumeration member whose {@link #getName()} matches specified assigned name.");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("*");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @param name YANG assigned name");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @return corresponding ");
        _builder.append(archetype.simpleName(), "     ");
        _builder.append(" item");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("* @throws ");
        _builder.append(npe, "     ");
        _builder.append(" if {@code name} is null");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("* @throws ");
        _builder.append(iae, "     ");
        _builder.append(" if {@code name} does not match any item");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("public static ");
        _builder.append(nonnullSelf, "    ");
        _builder.append(" ofName(");
        _builder.append(string, "    ");
        _builder.append(" name) {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("return ");
        _builder.append(codeHelpers, "        ");
        _builder.append(".checkEnum(forName(name), name);");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("/**");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* Return the enumeration member whose {@link #getIntValue()} matches specified value.");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("*");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @param intValue integer value");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* @return corresponding ");
        _builder.append(archetype.simpleName(), "     ");
        _builder.append(" item");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("* @throws ");
        _builder.append(iae, "     ");
        _builder.append(" if {@code intValue} does not match any item");
        _builder.newLineIfNotEmpty();
        _builder.append("     ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("public static ");
        _builder.append(nonnullSelf, "    ");
        _builder.append(" ofValue(int intValue) {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("return ");
        _builder.append(codeHelpers, "        ");
        _builder.append(".checkEnum(forValue(intValue), intValue);");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        return _builder;
    }

    private String writeValues() {
        final var values = archetype.values();
        if (values.isEmpty()) {
            return "";
        }

        final var it = values.iterator();
        final var sb = new StringBuilder();
        while (true) {
            final var value = it.next();

            value.getDescription().ifPresent(desc -> {
                final var doc = encodeJavadocSymbols(BindingGeneratorUtil.encodeAngleBrackets(desc.trim()));
                if (!doc.isEmpty()) {
                    sb.append(wrapToDocumentation(doc)).append('\n');
                }
            });
            sb.append(value.constantName()).append('(').append(value.value()).append(", \"").append(value.name())
                .append("\")");

            if (!it.hasNext()) {
                return sb.append(";\n").toString();
            }
            sb.append(",\n");
        }
    }
}
