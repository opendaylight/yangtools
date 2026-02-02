/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;

public final class EmptyPatternEffectiveStatement extends DefaultArgument<PatternExpression, @NonNull PatternStatement>
        implements PatternEffectiveStatement, ConstraintMetaDefinition.Mixin<PatternEffectiveStatement> {
    public EmptyPatternEffectiveStatement(final @NonNull PatternStatement declared) {
        super(declared);
    }

    @Override
    public PatternEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
