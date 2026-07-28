/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import org.opendaylight.yangtools.binding.model.api.RpcArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * A {@link RuntimeType} associated with an {@code rpc} statement.
 */
public interface RpcRuntimeType extends InvokableRuntimeType {
    // FIXME: it should be safe to have this as a default method
    @Override
    RpcEffectiveStatement statement();

    @Override
    RpcArchetype javaType();
}
