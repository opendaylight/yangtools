/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.Restrictions;

/**
 * Abstract base class holding pure-Java parts of {@link ClassTemplate}.
 */
abstract class AbstractClassTemplate extends BaseTemplate {
    private static final Comparator<GeneratedProperty> PROP_COMPARATOR =
        Comparator.comparing(GeneratedProperty::getName);

    final @NonNull GeneratedTransferObject genTO;
    final @NonNull List<GeneratedProperty> properties;
    final @NonNull List<GeneratedProperty> finalProperties;
    final @NonNull List<GeneratedProperty> parentProperties;
    final @NonNull List<GeneratedProperty> allProperties;
    final Restrictions restrictions;

    @NonNullByDefault
    AbstractClassTemplate(final AbstractJavaGeneratedType javaType, final GeneratedTransferObject genType) {
        super(javaType, genType);
        genTO = requireNonNull(genType);
        properties = genType.getProperties();
        finalProperties = GeneratorUtil.resolveReadOnlyPropertiesFromTO(genTO.getProperties());
        parentProperties = GeneratorUtil.getPropertiesOfAllParents(genTO);
        restrictions = genType.getRestrictions();

        allProperties = Stream.concat(properties.stream(), parentProperties.stream())
            .sorted(PROP_COMPARATOR)
            .collect(Collectors.toUnmodifiableList());
    }

    // FIXME: this method should live in (the now non-existent) BitsTypeObjectTemplate
    final String bitsDefaultInstanceBody() {
        final var sb = new StringBuilder()
            .append("var values = ").append(importedName(CODEHELPERS)).append(".parseBitsDefaultValue(defaultValue, ");
        final var size = allProperties.size();
        if (size != 0) {
            final var it = allProperties.iterator();
            while (true) {
                final var prop = it.next();
                sb.append('"').append(prop.getName()).append('"');
                if (!it.hasNext()) {
                    break;
                }
                sb.append(",\n    ");
            }
        }

        sb
            .append(");\n")
            .append("return new ").append(genTO.simpleName()).append("(");
        if (size != 0) {
            sb.append('\n');

            final var last = size - 1;
            for (int i = 0; i < last; ++i) {
                appendValue(sb, i);
                sb.append(",\n");
            }
            appendValue(sb, last);
        }

        return sb.append(");\n").toString();
    }

    @NonNullByDefault
    private static void appendValue(final StringBuilder sb, final int index) {
        sb.append("    values[").append(index).append(']');
    }
}
