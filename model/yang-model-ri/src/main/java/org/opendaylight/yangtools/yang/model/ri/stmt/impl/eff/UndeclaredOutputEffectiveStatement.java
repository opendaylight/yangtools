/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;

public final class UndeclaredOutputEffectiveStatement extends AbstractUndeclaredOperationContainer<OutputStatement>
        implements OutputEffectiveStatement, OutputSchemaNode {
    public UndeclaredOutputEffectiveStatement(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final QName argument, final int flags) {
        super(substatements, argument, flags);
    }

    public UndeclaredOutputEffectiveStatement(final UndeclaredOutputEffectiveStatement original, final QName argument,
            final int flags) {
        super(original, argument, flags);
    }

    @Override
    public OutputEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
