/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ConstraintMetaDefinitionMixin;

public final class EmptyLengthEffectiveStatement extends DefaultArgument<List<ValueRange>, LengthStatement>
        implements LengthEffectiveStatement, ConstraintMetaDefinitionMixin<List<ValueRange>, LengthStatement> {
    public EmptyLengthEffectiveStatement(final LengthStatement declared) {
        super(declared);
    }
}
