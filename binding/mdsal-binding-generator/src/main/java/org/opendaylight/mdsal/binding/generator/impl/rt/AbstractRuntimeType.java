/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract class AbstractRuntimeType<S extends EffectiveStatement<?, ?>, T extends Type> implements RuntimeType {
    private final @NonNull T javaType;
    private final @NonNull S statement;

    AbstractRuntimeType(final T bindingType, final S statement) {
        this.javaType = requireNonNull(bindingType);
        this.statement = requireNonNull(statement);
    }

    @Override
    public final T javaType() {
        return javaType;
    }

    @Override
    public final S statement() {
        return statement;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("javaType", javaType).add("statement", statement).toString();
    }
}
