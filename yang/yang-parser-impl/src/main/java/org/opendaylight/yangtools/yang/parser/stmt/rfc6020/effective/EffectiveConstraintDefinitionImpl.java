/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.data.util.ConstraintDefinitions;
import org.opendaylight.yangtools.yang.data.util.EmptyConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementBase;

final class EffectiveConstraintDefinitionImpl implements ConstraintDefinition {
    private static final String UNBOUNDED_STR = "unbounded";

    private final Integer minElements;
    private final Integer maxElements;

    private EffectiveConstraintDefinitionImpl(final Integer minElements, final Integer maxElements) {
        this.minElements = minElements;
        this.maxElements = maxElements;
    }

    static ConstraintDefinition forParent(final EffectiveStatementBase<?, ?> parent) {
        final MinElementsEffectiveStatement firstMinElementsStmt = parent
                .firstEffective(MinElementsEffectiveStatement.class);
        final Integer minElements;
        if (firstMinElementsStmt != null) {
            final Integer m = firstMinElementsStmt.argument();
            minElements = m > 0 ? m : null;
        } else {
            minElements = null;
        }

        final MaxElementsEffectiveStatement firstMaxElementsStmt = parent
                .firstEffective(MaxElementsEffectiveStatement.class);
        final String maxElementsArg = firstMaxElementsStmt == null ? UNBOUNDED_STR : firstMaxElementsStmt.argument();
        final Integer maxElements;
        if (!UNBOUNDED_STR.equals(maxElementsArg)) {
            final Integer m = Integer.valueOf(maxElementsArg);
            maxElements = m < Integer.MAX_VALUE ? m : null;
        } else {
            maxElements = null;
        }

        // Check for singleton instances
        if (minElements == null && maxElements == null) {
            return EmptyConstraintDefinition.getInstance();
        }

        return new EffectiveConstraintDefinitionImpl(minElements, maxElements);
    }

    @Override
    public Integer getMinElements() {
        return minElements;
    }

    @Override
    public Integer getMaxElements() {
        return maxElements;
    }

    @Override
    public int hashCode() {
        return ConstraintDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return ConstraintDefinitions.equals(this, obj);
    }

    @Override
    public String toString() {
        return ConstraintDefinitions.toString(this);
    }
}
