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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;

public final class EmptyGroupingEffectiveStatement extends AbstractGroupingEffectiveStatement {
    public EmptyGroupingEffectiveStatement(final GroupingStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(declared, substatements, flags);
    }

    @Override
    public QName argument() {
        return getDeclared().argument();
    }
}
