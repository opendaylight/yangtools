/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import org.opendaylight.yangtools.binding.model.api.KeyedListActionArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * A {@link RuntimeType} associated with an {@code action} statement defined in a {@code list} with a {@code key}.
 *
 * @since 16.0.0
 */
public interface KeyedListActionRuntimeType extends InvokableRuntimeType {
    @Override
    ActionEffectiveStatement statement();

    @Override
    KeyedListActionArchetype javaType();
}
