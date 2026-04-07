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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Template for {@link DataRoot} specializations.
 */
final class DataRootTemplate extends InterfaceTemplate {
    // TODO: allow sharing of the following between method invocations:
    //    final var rootMetaType = BindingTypes.rootMeta(archetype);
    //    final var rootMetaRaw = importedName(rootMetaType.getRawType());
    //    final var type = archetype.selfReference();

    @NonNullByDefault
    DataRootTemplate(final DataRootArchetype archetype) {
        super(archetype);
    }

    private @NonNull DataRootArchetype archetype() {
        return (DataRootArchetype) type();
    }

    @Override
    BlockBuilder generateConstants() {
        final var archetype = archetype();

        // pre-compute constants: split out for future isolation
        final var nonNullByDefault = importedName(NONNULL_BY_DEFAULT);
        final var rootMetaType = BindingTypes.rootMeta(archetype);
        final var rootMetaRaw = importedName(rootMetaType.getRawType());
        final var moduleInfo = importedName(archetype.yangModuleInfo());
        // FIXME: YANGTOOLS-1808: use importedName()
        final var type = archetype.canonicalName();

        return newBlockBuilder()
            .eol("/**")
            .str(" * The {@link ").str(rootMetaRaw).eol("} associated with this module root.")
            .eol(" */")
            .at().eol(nonNullByDefault)
            // FIXME: YANGTOOLS-1808: use importedName() on rootMetaType
            .str(rootMetaRaw).str("<").str(type).str("> META = new ").str(rootMetaRaw).str("<>(").str(type)
                .str(".class, ").str(moduleInfo).eol('.' + MODULE_INFO_INSTANCE_FIELD_NAME + ");");
    }

    @Override
    BlockBuilder generateMethods() {
        final var archetype = archetype();

        // pre-compute constants: split out for future isolation
        final var rootMetaType = BindingTypes.rootMeta(archetype);
        final var rootMeta = importedName(rootMetaType.getRawType());
        final var override = importedName(OVERRIDE);
        final var type = archetype.canonicalName();

        final var bb = newBlockBuilder()
            .blk(generateDefaultImplementedInterface())
            .nl()
            .at().eol(override)
            // FIXME: YANGTOOLS-1808: use importedName() on rootMetaType
            .str("default ").gen(rootMeta, type).str(" " + DATA_ROOT_META_NAME + "()").oB()
                .eol("return " + META_STATIC_FIELD_NAME + ';')
            .cB();

        final var superMethods = super.generateMethods();
        if (superMethods != null) {
            bb.nl().blk(superMethods);
        }
        return bb;
    }
}
