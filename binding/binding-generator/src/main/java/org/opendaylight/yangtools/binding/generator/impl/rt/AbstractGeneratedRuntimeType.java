/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.rt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract class AbstractGeneratedRuntimeType<S extends EffectiveStatement<?, ?>> extends StmtRuntimeType<S, Archetype> {
    @NonNullByDefault
    AbstractGeneratedRuntimeType(final Archetype bindingType, final S statement) {
        super(bindingType, statement);
    }
}
