/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.must;

import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.ConstraintMetaDefinitionMixin;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

interface MustDefinitionMixin extends MustDefinition, ConstraintMetaDefinitionMixin<QualifiedBound, MustStatement>,
        MustEffectiveStatement {
    @Override
    default MustEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
