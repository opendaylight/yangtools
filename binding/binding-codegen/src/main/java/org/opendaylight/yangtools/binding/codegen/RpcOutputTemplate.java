/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Template for {@link RpcOutput} specializations.
 */
@NonNullByDefault
final class RpcOutputTemplate extends AugmentableTemplate<RpcOutputArchetype>
        implements BuilderTemplate.TargetTemplate {
    private static final ConcreteType RPC_OUTPUT = ConcreteType.ofClass(RpcOutput.class);

    RpcOutputTemplate(final DataRootArchetype root, final RpcOutputArchetype archetype) {
        super(root, archetype);
    }

    @Override
    public RpcOutputTemplate self() {
        return this;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(ParameterizedType.of(RPC_OUTPUT, archetype)),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
