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

    private EnumTypeObjectTemplate(final AbstractJavaGeneratedType javaType, final EnumTypeObjectArchetype archetype) {
        super(javaType, archetype);
        this.archetype = requireNonNull(archetype);
    }

    EnumTypeObjectTemplate(final EnumTypeObjectArchetype archetype) {
        super(archetype);
        this.archetype = requireNonNull(archetype);
    }

    static String generateAsInner(final AbstractJavaGeneratedType javaType, final EnumTypeObjectArchetype archetype) {
        return new EnumTypeObjectTemplate(javaType, archetype).body();
    }

    @Override
    String body() {
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
        final var sb = new StringBuilder();

        final var javadoc = formatDataForJavaDoc(archetype);
        if (!javadoc.isBlank()) {
            sb.append(wrapToDocumentation(javadoc)).append('\n');
        }

        sb.append(generatedAnnotation()).append('\n');
        sb.append("public enum ").append(typeName).append(" implements ").append(enumTypeObject).append(" {\n");

        final var it = archetype.values().iterator();
        if (it.hasNext()) {
            while (true) {
                final var value = it.next();

                value.getDescription().ifPresent(desc -> {
                    final var doc = encodeJavadocSymbols(BindingGeneratorUtil.encodeAngleBrackets(desc.trim()));
                    if (!doc.isEmpty()) {
                        // deal with indentation in multi-line string via StringConcatenation for now
                        // TODO: wrapToDocumentation() should learn to do this for us
                        final var sc = new StringConcatenation();
                        sc.append("    ");
                        sc.append(wrapToDocumentation(doc), "    ");
                        sb.append(sc).append('\n');
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

        sb.append("    private final ").append(nonnullString).append(" name;\n");
        sb.append("    private final int value;\n\n");

        sb.append("    private ").append(typeName).append("(int value, ").append(nonnullString).append(" name) {\n");
        sb.append("        this.value = value;\n");
        sb.append("        this.name = name;\n");
        sb.append("    }\n\n");

        sb.append("    @").append(override).append('\n');
        sb.append("    public ").append(nonnullString).append(" getName() {\n");
        sb.append("        return name;\n");
        sb.append("    }\n\n");

        sb.append("    @").append(override).append('\n');
        sb.append("    public int getIntValue() {\n");
        sb.append("        return value;\n");
        sb.append("    }\n\n");

        sb.append("    /**\n");
        sb.append("     * Return the enumeration member whose {@link #getName()} matches specified assigned name.\n");
        sb.append("     *\n");
        sb.append("     * @param name YANG assigned name\n");
        sb.append("     * @return corresponding ").append(typeName)
            .append(" item, or {@code null} if no such item exists\n");
        sb.append("     * @throws ").append(npe).append(" if {@code name} is null\n");
        sb.append("     */\n");
        sb.append("    public static ").append(nullableSelf).append(" forName(").append(string).append(" name) {\n");
        sb.append("        return switch (name) {\n");
        for (var value : archetype.values()) {
            sb.append("            case \"").append(value.name()).append("\" -> ").append(value.constantName())
                .append(";\n");
        }
        sb.append("            default -> null;\n");
        sb.append("        };\n");
        sb.append("    }\n\n");

        sb.append("    /**\n");
        sb.append("     * Return the enumeration member whose {@link #getIntValue()} matches specified value.\n");
        sb.append("     *\n");
        sb.append("     * @param intValue integer value\n");
        sb.append("     * @return corresponding ").append(typeName)
            .append(" item, or {@code null} if no such item exists\n");
        sb.append("     */\n");
        sb.append("    public static ").append(nullableSelf).append(" forValue(int intValue) {\n");
        sb.append("        return switch (intValue) {\n");
        for (var value : archetype.values()) {
            sb.append("            case ").append(value.value()).append(" -> ").append(value.constantName())
                .append(";\n");
        }
        sb.append("            default -> null;\n");
        sb.append("        };\n");
        sb.append("    }\n\n");

        sb.append("    /**\n");
        sb.append("     * Return the enumeration member whose {@link #getName()} matches specified assigned name.\n");
        sb.append("     *\n");
        sb.append("     * @param name YANG assigned name\n");
        sb.append("     * @return corresponding ").append(typeName).append(" item\n");
        sb.append("     * @throws ").append(npe).append(" if {@code name} is null\n");
        sb.append("     * @throws ").append(iae).append(" if {@code name} does not match any item\n");
        sb.append("     */\n");
        sb.append("    public static ").append(nonnullSelf).append(" ofName(").append(string).append(" name) {\n");
        sb.append("        return ").append(codeHelpers).append(".checkEnum(forName(name), name);\n");
        sb.append("    }\n\n");

        sb.append("    /**\n");
        sb.append("     * Return the enumeration member whose {@link #getIntValue()} matches specified value.\n");
        sb.append("     *\n");
        sb.append("     * @param intValue integer value\n");
        sb.append("     * @return corresponding ").append(typeName).append(" item\n");
        sb.append("     * @throws ").append(iae).append(" if {@code intValue} does not match any item\n");
        sb.append("     */\n");
        sb.append("    public static ").append(nonnullSelf).append(" ofValue(int intValue) {\n");
        sb.append("        return ").append(codeHelpers).append(".checkEnum(forValue(intValue), intValue);\n");
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }
}
