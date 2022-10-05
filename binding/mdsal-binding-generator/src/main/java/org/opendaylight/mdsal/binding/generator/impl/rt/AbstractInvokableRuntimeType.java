/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InvokableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract sealed class AbstractInvokableRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractCompositeRuntimeType<S> implements InvokableRuntimeType
        permits DefaultActionRuntimeType, DefaultRpcRuntimeType {
    private final @NonNull InputRuntimeType input;
    private final @NonNull OutputRuntimeType output;

    AbstractInvokableRuntimeType(final GeneratedType bindingType, final S statement, final List<RuntimeType> children) {
        super(bindingType, statement, children);
        input = child(children, InputRuntimeType.class);
        output = child(children, OutputRuntimeType.class);
    }

    @Override
    public final InputRuntimeType input() {
        return input;
    }

    @Override
    public final OutputRuntimeType output() {
        return output;
    }

    private static <T extends RuntimeType> @NonNull T child(final List<RuntimeType> list, final Class<T> clazz) {
        return list.stream().filter(clazz::isInstance).map(clazz::cast).findFirst().orElseThrow();
    }
}
