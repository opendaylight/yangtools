/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.generator.impl.tree.SchemaTreeChild;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Abstract base class for {@link AbstractCompositeGenerator}s which are also {@link SchemaTreeChild}ren.
 */
abstract class CompositeSchemaTreeGenerator<S extends SchemaTreeEffectiveStatement<?>,
        G extends CompositeSchemaTreeGenerator<S, G>>
        extends AbstractCompositeGenerator<S> implements SchemaTreeChild<S, G> {
    CompositeSchemaTreeGenerator(final S statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final G generator() {
        return (G) this;
    }
}
