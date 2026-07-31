/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.CONST_UNSAFE_ACCESS;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.INSTANCE_FIELD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.yangModuleInfoOf;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Template for {@link DataRoot} specializations.
 */
@NonNullByDefault
final class DataRootTemplate extends InterfaceTemplate<DataRootArchetype> {
    record Builder(DataRootArchetype type) implements Template.Builder {
        Builder {
            requireNonNull(type);
        }

        @Override
        public DataRootTemplate build() {
            return new DataRootTemplate(type);
        }
    }

    private static final JavaTypeName ROOT_META = JavaTypeName.create(RootMeta.class);
    private static final ConcreteType DATA_ROOT = ConcreteType.ofClass(DataRoot.class);

    private DataRootTemplate(final DataRootArchetype archetype) {
        super(archetype, archetype, true, false, false);
    }

    @Override
    @Nullable DataRootArchetype builderTarget() {
        return null;
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
            final var moduleInfo = importedName(yangModuleInfoOf(archetype.statement().localQNameModule()));
            // FIXME: YANGTOOLS-1808: use selfRef()
            final var type = archetype.canonicalName();

            bb
                .eol("/**")
                .str(" * The {@link ").str(rootMeta).eol("} associated with this module root.")
                .eol(" */")
                .at().eol(importedName(NONNULL_BY_DEFAULT))
                .gen(rootMeta, type).str(" META = new ").str(rootMeta).str("<>(").str(type).str(".class, ")
                    .str(moduleInfo).str('.' + INSTANCE_FIELD_NAME + ", ")
                    .str(moduleInfo).eol('.' + CONST_UNSAFE_ACCESS + ");");
        };
    }
}
