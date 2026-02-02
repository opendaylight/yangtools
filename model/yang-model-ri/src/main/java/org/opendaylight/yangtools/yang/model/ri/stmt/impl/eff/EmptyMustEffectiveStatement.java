/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

public final class EmptyMustEffectiveStatement extends DefaultArgument<QualifiedBound, @NonNull MustStatement>
        implements MustEffectiveStatement, MustDefinition {
    public EmptyMustEffectiveStatement(final @NonNull MustStatement declared) {
        super(declared);
    }

    @Override
    public MustEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
