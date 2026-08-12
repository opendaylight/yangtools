/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NONNULL_BY_DEFAULT;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.CONST_UNSAFE_ACCESS;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.INSTANCE_FIELD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.yangModuleInfoOf;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;

/**
 * Template for {@link DataRoot} specializations.
 */
@NonNullByDefault
final class DataRootTemplate extends InterfaceTemplate<DataRootArchetype> {
    private static final TypeName ROOT_META = TypeName.ofClass(RootMeta.class);
    private static final ConcreteType DATA_ROOT = ConcreteType.ofClass(DataRoot.class);

    private DataRootTemplate(final DataRootArchetype archetype) {
        super(archetype, archetype);
    }

    static DataRootTemplate of(final DataRootArchetype root, final DataRootArchetype archetype) {
        verify(root.equals(archetype));
        return new DataRootTemplate(archetype);
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(ParameterizedType.of(DATA_ROOT, archetype)),
            super.extendsTypes());
    }

    @Override
    BlockFragment constants() {
        return bb -> {
            final var rootMeta = importedName(ROOT_META);
            final var moduleInfo = importedName(yangModuleInfoOf(root));
            // FIXME: YANGTOOLS-1808: use selfRef()
            final var type = archetype.canonicalName();

            bb
                .eol("/**")
                .str(" * The {@link ").str(rootMeta).eol("} associated with this module root.")
                .eol(" */")
                .at().eol(importedName(NONNULL_BY_DEFAULT))
                .str(rootMeta).lt().str(type).gt().str(" META = new ").str(rootMeta).str("<>(")
                    .str(type).str(".class, ")
                    .str(moduleInfo).str('.' + INSTANCE_FIELD_NAME + ", ")
                    .str(moduleInfo).eol('.' + CONST_UNSAFE_ACCESS + ");");
        };
    }

    @Override
    BlockBuilder contractMethods(final BlockBuilder bb) {
        return bb
            .nl()
            .frg(new ImplementedInterfaceMethod.Canonical(this));
    }
}
