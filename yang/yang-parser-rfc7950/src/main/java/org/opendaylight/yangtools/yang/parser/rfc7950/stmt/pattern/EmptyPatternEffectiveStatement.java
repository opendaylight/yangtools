/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.ConstraintMetaDefinitionMixin;

final class EmptyPatternEffectiveStatement extends DefaultArgument<PatternConstraint, PatternStatement>
        implements PatternEffectiveStatement, ConstraintMetaDefinitionMixin<PatternConstraint, PatternStatement> {
    EmptyPatternEffectiveStatement(final PatternStatement declared) {
        super(declared);
    }
}
