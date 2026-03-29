/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.contract.Naming.DATA_ROOT_META_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.META_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.MODULE_INFO_INSTANCE_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataRoot;
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
    StringBuilder generateConstants() {
        final var archetype = archetype();

        // pre-compute constants: split out for future isolation
        final var nonNullByDefault = importedName(NONNULL_BY_DEFAULT);
        final var rootMetaType = BindingTypes.rootMeta(archetype);
        final var rootMetaRaw = importedName(rootMetaType.getRawType());
        final var moduleInfo = importedName(archetype.yangModuleInfo());
        // FIXME: YANGTOOLS-1808: use importedName()
        final var type = archetype.canonicalName();

        return new StringBuilder(
             "/**\n"
            +  " * The {@link ").append(rootMetaRaw).append("} associated with this module root.\n").append(
               " */\n"
            +  '@').append(nonNullByDefault).append('\n')
            // FIXME: YANGTOOLS-1808: use importedName() on rootMetaType
            .append(rootMetaRaw).append('<').append(type).append("> META = new ").append(rootMetaRaw).append("<>(")
                .append(type).append(".class, ").append(moduleInfo)
                .append('.' + MODULE_INFO_INSTANCE_FIELD_NAME + ");\n");
    }

    @Override
    BlockBuilder generateMethods() {
        final var archetype = archetype();

        // pre-compute constants: split out for future isolation
        final var rootMetaType = BindingTypes.rootMeta(archetype);
        final var rootMeta = importedName(rootMetaType.getRawType());
        final var override = importedName(OVERRIDE);
        final var type = archetype.canonicalName();

        final var bb = new BlockBuilder();
        bb.append(generateDefaultImplementedInterface());
        bb
            .nl()
            .at().str(override).nl()
            // FIXME: YANGTOOLS-1808: use importedName() on rootMetaType
            .str("default ").str(rootMeta).str("<").str(type).str("> " + DATA_ROOT_META_NAME + "() {").nl()
            .str("    return " + META_STATIC_FIELD_NAME + ';').nl()
            .append("}\n");

        final var superMethods = super.generateMethods();
        if (superMethods != null) {
            bb.nl().append(superMethods);
        }
        return bb;
    }
}
