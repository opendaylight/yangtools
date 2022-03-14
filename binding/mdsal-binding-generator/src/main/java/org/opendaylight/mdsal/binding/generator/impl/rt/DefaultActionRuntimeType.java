/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

@Beta
public final class DefaultActionRuntimeType extends AbstractCompositeRuntimeType<ActionEffectiveStatement>
        implements ActionRuntimeType {
    private final @NonNull InputRuntimeType input;
    private final @NonNull OutputRuntimeType output;

    public DefaultActionRuntimeType(final GeneratedType bindingType, final ActionEffectiveStatement statement,
            final List<RuntimeType> children) {
        super(bindingType, statement, children);
        input = child(children, InputRuntimeType.class);
        output = child(children, OutputRuntimeType.class);
    }

    @Override
    public InputRuntimeType input() {
        return input;
    }

    @Override
    public OutputRuntimeType output() {
        return output;
    }

    private static <T extends RuntimeType> @NonNull T child(final List<RuntimeType> list, final Class<T> clazz) {
        return list.stream().filter(clazz::isInstance).map(clazz::cast).findFirst().orElseThrow();
    }
}
