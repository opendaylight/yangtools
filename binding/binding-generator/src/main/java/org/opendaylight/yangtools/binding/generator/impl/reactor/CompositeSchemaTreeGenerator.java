/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Abstract base class for {@link AbstractCompositeGenerator}s which are also {@link SchemaTreeChild}ren.
 */
abstract class CompositeSchemaTreeGenerator<S extends SchemaTreeEffectiveStatement<?>, R extends CompositeRuntimeType>
        extends AbstractCompositeGenerator<S, R> {
    CompositeSchemaTreeGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }
}
