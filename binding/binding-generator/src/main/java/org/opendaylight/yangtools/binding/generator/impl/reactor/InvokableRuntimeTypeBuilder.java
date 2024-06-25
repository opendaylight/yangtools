/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract class InvokableRuntimeTypeBuilder<S extends EffectiveStatement<?, ?>, R extends CompositeRuntimeType>
        extends CompositeRuntimeTypeBuilder<S, R> {
    InvokableRuntimeTypeBuilder(final S statement) {
        super(statement);
    }

    @Override
    final R build(final GeneratedType type, final S statement, final List<RuntimeType> children,
            final List<AugmentRuntimeType> augments) {
        verify(augments.isEmpty(), "Unexpected augments %s", augments);
        return build(type, statement, children);
    }

    abstract @NonNull R build(GeneratedType type, S statement, List<RuntimeType> children);
}
