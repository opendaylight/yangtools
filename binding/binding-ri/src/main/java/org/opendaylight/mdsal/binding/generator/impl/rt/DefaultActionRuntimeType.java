/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

public final class DefaultActionRuntimeType extends AbstractInvokableRuntimeType<ActionEffectiveStatement>
        implements ActionRuntimeType {
    public DefaultActionRuntimeType(final GeneratedType bindingType, final ActionEffectiveStatement statement,
            final List<RuntimeType> children) {
        super(bindingType, statement, children);
    }
}
