/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    private EnumTypeObjectTemplate(final AbstractJavaGeneratedType javaType, final EnumTypeObjectArchetype archetype) {
        super(javaType, archetype);
        this.archetype = requireNonNull(archetype);
    }

    EnumTypeObjectTemplate(final EnumTypeObjectArchetype archetype) {
        super(archetype);
        this.archetype = requireNonNull(archetype);
    }

    static void generateAsInner(final AbstractJavaGeneratedType javaType, final EnumTypeObjectArchetype archetype,
            final StringBuilder sb) {
        new EnumTypeObjectTemplate(javaType, archetype).appendBody(sb);
    }

    @Override
    String body() {
        final var sb = new StringBuilder();
        appendBody(sb);
        return sb.toString();
    }

    private void appendBody(final StringBuilder sb) {
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
        final var typeName = archetype.simpleName();

        // now build the body
        final var javadoc = formatDataForJavaDoc(archetype);
        if (!javadoc.isBlank()) {
            sb.append(wrapToDocumentation(javadoc)).append('\n');
        }

        sb
            .append(generatedAnnotation()).append('\n')
            .append("public enum ").append(typeName).append(" implements ").append(enumTypeObject).append(" {\n");

        final var it = archetype.values().iterator();
        if (it.hasNext()) {
            while (true) {
                final var value = it.next();

                value.getDescription().ifPresent(desc -> {
                    final var doc = encodeJavadocSymbols(BindingGeneratorUtil.encodeAngleBrackets(desc.trim()));
                    if (!doc.isEmpty()) {
                        appendAsJavadoc(sb, "    ", doc);
                        sb.append('\n');
                    }
                });
                sb.append("    ").append(value.constantName()).append('(').append(value.value()).append(", \"")
                    .append(value.name()).append("\")");

                if (!it.hasNext()) {
                    break;
                }

                sb.append(",\n");
            }

            sb.append(";\n\n");
        }

        sb
            .append("    private final ").append(nonnullString).append(" name;\n")
            .append("    private final int value;\n\n")

            .append("    private ").append(typeName).append("(int value, ").append(nonnullString).append(" name) {\n")
            .append("        this.value = value;\n")
            .append("        this.name = name;\n")
            .append("    }\n\n")

            .append("    @").append(override).append('\n')
            .append("    public ").append(string).append(" getName() {\n")
            .append("        return name;\n")
            .append("    }\n\n")

            .append("    @").append(override).append('\n')
            .append("    public int getIntValue() {\n")
            .append("        return value;\n")
            .append("    }\n\n")

            .append("    /**\n")
            .append("     * Return the enumeration member whose {@link #getName()} matches specified assigned name.\n")
            .append("     *\n")
            .append("     * @param name YANG assigned name\n")
            .append("     * @return corresponding ").append(typeName)
                .append(" item, or {@code null} if no such item exists\n")
                .append("     * @throws ").append(npe).append(" if {@code name} is null\n")
            .append("     */\n")
            .append("    public static ").append(nullableSelf).append(" forName(").append(string).append(" name) {\n")
            .append("        return switch (name) {\n");
        for (var value : archetype.values()) {
            sb.append("            case \"").append(value.name()).append("\" -> ").append(value.constantName())
                .append(";\n");
        }
        sb
            .append("            default -> null;\n")
            .append("        };\n")
            .append("    }\n\n")

            .append("    /**\n")
            .append("     * Return the enumeration member whose {@link #getIntValue()} matches specified value.\n")
            .append("     *\n")
            .append("     * @param intValue integer value\n")
            .append("     * @return corresponding ").append(typeName)
                .append(" item, or {@code null} if no such item exists\n")
            .append("     */\n")
            .append("    public static ").append(nullableSelf).append(" forValue(int intValue) {\n")
            .append("        return switch (intValue) {\n");
        for (var value : archetype.values()) {
            sb.append("            case ").append(value.value()).append(" -> ").append(value.constantName())
                .append(";\n");
        }
        sb
            .append("            default -> null;\n")
            .append("        };\n")
            .append("    }\n\n")

            .append("    /**\n")
            .append("     * Return the enumeration member whose {@link #getName()} matches specified assigned name.\n")
            .append("     *\n")
            .append("     * @param name YANG assigned name\n")
            .append("     * @return corresponding ").append(typeName).append(" item\n")
            .append("     * @throws ").append(npe).append(" if {@code name} is null\n")
            .append("     * @throws ").append(iae).append(" if {@code name} does not match any item\n")
            .append("     */\n")
            .append("    public static ").append(nonnullSelf).append(" ofName(").append(string).append(" name) {\n")
            .append("        return ").append(codeHelpers).append(".checkEnum(forName(name), name);\n")
            .append("    }\n\n")

            .append("    /**\n")
            .append("     * Return the enumeration member whose {@link #getIntValue()} matches specified value.\n")
            .append("     *\n")
            .append("     * @param intValue integer value\n")
            .append("     * @return corresponding ").append(typeName).append(" item\n")
            .append("     * @throws ").append(iae).append(" if {@code intValue} does not match any item\n")
            .append("     */\n")
            .append("    public static ").append(nonnullSelf).append(" ofValue(int intValue) {\n")
            .append("        return ").append(codeHelpers).append(".checkEnum(forValue(intValue), intValue);\n")
            .append("    }\n")
            .append("}\n");
    }
}
