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
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Template for {@link RpcInput} specializations.
 */
@NonNullByDefault
final class RpcInputTemplate extends AugmentableTemplate<RpcInputArchetype> implements BuilderTemplate.TargetTemplate {
    private static final ConcreteType RPC_INPUT = ConcreteType.ofClass(RpcInput.class);

    RpcInputTemplate(final DataRootArchetype root, final RpcInputArchetype archetype) {
        super(root, archetype);
    }

    @Override
    public RpcInputArchetype builderTarget() {
        return archetype;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(ParameterizedType.of(RPC_INPUT, archetype)),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
