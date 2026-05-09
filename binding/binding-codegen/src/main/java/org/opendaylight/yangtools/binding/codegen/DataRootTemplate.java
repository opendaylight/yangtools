/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.CONST_UNSAFE_ACCESS;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.INSTANCE_FIELD_NAME;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.yangModuleInfoOf;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Template for {@link DataRoot} specializations.
 */
final class DataRootTemplate extends InterfaceTemplate {
    @NonNullByDefault
    record Builder(DataRootArchetype type) implements Template.Builder {
        Builder(final DataRootArchetype type) {
            this.type = requireNonNull(type);
        }

        @Override
        public DataRootTemplate build() {
            return new DataRootTemplate(type);
        }
    }

    @NonNullByDefault
    private DataRootTemplate(final DataRootArchetype archetype) {
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
        final var moduleInfo = importedName(yangModuleInfoOf(archetype.statement().localQNameModule()));

        // FIXME: YANGTOOLS-1808: use importedName()
        final var type = archetype.canonicalName();

        return newBlockBuilder()
            .eol("/**")
            .str(" * The {@link ").str(rootMetaRaw).eol("} associated with this module root.")
            .eol(" */")
            .at().eol(nonNullByDefault)
            // FIXME: YANGTOOLS-1808: use importedName() on rootMetaType
            .str(rootMetaRaw).str("<").str(type).str("> META = new ").str(rootMetaRaw).str("<>(").str(type)
                .str(".class, ").str(moduleInfo).str('.' + INSTANCE_FIELD_NAME + ", ")
                .str(moduleInfo).eol('.' + CONST_UNSAFE_ACCESS + ");");
    }
}
