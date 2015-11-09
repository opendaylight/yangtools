/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

final class EffectiveConstraintDefinitionImpl implements ConstraintDefinition {
    private static final Integer UNBOUNDED_INT = Integer.MAX_VALUE;
    private static final String UNBOUNDED_STR = "unbounded";
    private final RevisionAwareXPath whenCondition;
    private final Set<MustDefinition> mustConstraints;
    private final Integer minElements;
    private final Integer maxElements;
    private final boolean mandatory;

    private EffectiveConstraintDefinitionImpl(final boolean mandatory, final Integer minElements,
            final Integer maxElements, final RevisionAwareXPath whenCondition,
            final Set<MustDefinition> mustConstraints) {
        this.mandatory = mandatory;
        this.minElements = Preconditions.checkNotNull(minElements);
        this.maxElements = Preconditions.checkNotNull(maxElements);
        this.whenCondition = whenCondition;
        this.mustConstraints = Preconditions.checkNotNull(mustConstraints);
    }

    static ConstraintDefinition forParent(final EffectiveStatementBase<?, ?> parent) {
        final MinElementsEffectiveStatementImpl firstMinElementsStmt = parent
                .firstEffective(MinElementsEffectiveStatementImpl.class);
        final Integer minElements = (firstMinElementsStmt == null) ? 0 : firstMinElementsStmt.argument();

        final MaxElementsEffectiveStatementImpl firstMaxElementsStmt = parent
                .firstEffective(MaxElementsEffectiveStatementImpl.class);
        final String maxElementsArg = (firstMaxElementsStmt == null) ? UNBOUNDED_STR : firstMaxElementsStmt.argument();
        final Integer maxElements;
        if (UNBOUNDED_STR.equals(maxElementsArg)) {
            maxElements = UNBOUNDED_INT;
        } else {
            maxElements = Integer.valueOf(maxElementsArg);
        }

        final MandatoryEffectiveStatement firstMandatoryStmt = parent.firstEffective(MandatoryEffectiveStatement.class);
        final boolean mandatory = (firstMandatoryStmt == null) ? minElements > 0 : firstMandatoryStmt.argument();

        final Set<MustDefinition> mustSubstatements = ImmutableSet.copyOf(parent.allSubstatementsOfType(
            MustDefinition.class));
        final WhenEffectiveStatementImpl firstWhenStmt = parent.firstEffective(WhenEffectiveStatementImpl.class);

        // Check for singleton instances
        if (minElements == 0 && maxElements == UNBOUNDED_INT && mustSubstatements.isEmpty() && firstWhenStmt == null) {
            return EmptyConstraintDefinition.create(mandatory);
        }

        return new EffectiveConstraintDefinitionImpl(mandatory, minElements, maxElements,
            (firstWhenStmt == null) ? null : firstWhenStmt.argument(), mustSubstatements);
    }

    @Override
    public RevisionAwareXPath getWhenCondition() {
        return whenCondition;
    }

    @Override
    public Set<MustDefinition> getMustConstraints() {
        return mustConstraints;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
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
