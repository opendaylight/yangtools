/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Template for {@link DataRoot} specializations.
 */
final class DataRootTemplate extends InterfaceTemplate {
    DataRootTemplate(final DataRootArchetype archetype) {
        super(archetype);
    }

    private @NonNull DataRootArchetype archetype() {
        return (DataRootArchetype) type();
    }

    @Override
    String generateConstants() {
        final var archetype = archetype();

        // pre-compute constants: split out for future isolation
        final var nonNullByDefault = importedName(NONNULL_BY_DEFAULT);
        final var rootMetaType = BindingTypes.rootMeta(archetype);
        final var rootMetaRaw = importedName(rootMetaType.getRawType());
        final var rootMeta = importedName(rootMetaType);
        final var moduleInfo = importedName(archetype.yangModuleInfo());
        final var type = importedName(archetype);

        return "/**\n"
            +  " * The {@link " + rootMetaRaw + "} associated with this module root.\n"
            +  " */\n"
            +  '@' + nonNullByDefault + '\n'
            +  rootMeta + " META = new " + rootMetaRaw + "<>(" + type + ".class, " + moduleInfo + ".INSTANCE);\n";
    }

    @Override
    String generateMethods() {
        final var archetype = archetype();

        // pre-compute constants: split out for future isolation
        final var rootMetaType = importedName(BindingTypes.rootMeta(archetype));
        final var override = importedName(OVERRIDE);

        final var sb = new StringBuilder()
            .append(generateDefaultImplementedInterface()).append('\n')
            .append('@').append(override).append('\n')
            .append("default ").append(rootMetaType).append(' ').append(Naming.DATA_ROOT_META_NAME).append("() {\n")
            .append("    return ").append(Naming.META_STATIC_FIELD_NAME).append(";\n")
            .append("}\n");

        final var superMethods = super.generateMethods();
        if (!superMethods.isEmpty()) {
            sb.append('\n').append(superMethods);
        }
        return sb.toString();
    }
}
