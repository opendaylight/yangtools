/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.RpcRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

public final class DefaultRpcRuntimeType extends AbstractInvokableRuntimeType<RpcEffectiveStatement>
        implements RpcRuntimeType {
    public DefaultRpcRuntimeType(final GeneratedType bindingType, final RpcEffectiveStatement statement,
            final List<RuntimeType> children) {
        super(bindingType, statement, children);
    }
}
