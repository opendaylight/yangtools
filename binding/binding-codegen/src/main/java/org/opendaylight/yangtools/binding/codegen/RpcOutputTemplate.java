/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Template for {@link RpcOutput} specializations.
 */
@NonNullByDefault
final class RpcOutputTemplate extends AugmentableTemplate<RpcOutputArchetype> {
    record Builder(RpcOutputArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public RpcOutputTemplate build() {
            return new RpcOutputTemplate(type, root);
        }
    }

    private static final ConcreteType RPC_OUTPUT = ConcreteType.ofClass(RpcOutput.class);

    private RpcOutputTemplate(final RpcOutputArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    @NonNull RpcOutputArchetype builderTarget() {
        return archetype;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.forArray(RPC_OUTPUT, extendsAugmentable(), extendsJavaDataContainer()),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
