/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.util.ConstraintDefinitions;
import org.opendaylight.yangtools.yang.data.util.EmptyConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

final class EffectiveConstraintDefinitionImpl implements ConstraintDefinition {
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
        this.minElements = minElements;
        this.maxElements = maxElements;
        this.whenCondition = whenCondition;
        this.mustConstraints = requireNonNull(mustConstraints);
    }

    static ConstraintDefinition forParent(final EffectiveStatementBase<?, ?> parent) {
        final MinElementsEffectiveStatementImpl firstMinElementsStmt = parent
                .firstEffective(MinElementsEffectiveStatementImpl.class);
        final Integer minElements;
        if (firstMinElementsStmt != null) {
            final Integer m = firstMinElementsStmt.argument();
            minElements = m > 0 ? m : null;
        } else {
            minElements = null;
        }

        final MaxElementsEffectiveStatementImpl firstMaxElementsStmt = parent
                .firstEffective(MaxElementsEffectiveStatementImpl.class);
        final String maxElementsArg = firstMaxElementsStmt == null ? UNBOUNDED_STR : firstMaxElementsStmt.argument();
        final Integer maxElements;
        if (!UNBOUNDED_STR.equals(maxElementsArg)) {
            final Integer m = Integer.valueOf(maxElementsArg);
            maxElements = m < Integer.MAX_VALUE ? m : null;
        } else {
            maxElements = null;
        }

        final MandatoryEffectiveStatement firstMandatoryStmt = parent.firstEffective(MandatoryEffectiveStatement.class);
        final boolean mandatory = firstMandatoryStmt == null ? minElements != null : firstMandatoryStmt.argument();

        final Set<MustDefinition> mustSubstatements = ImmutableSet.copyOf(parent.allSubstatementsOfType(
            MustDefinition.class));
        final WhenEffectiveStatementImpl firstWhenStmt = parent.firstEffective(WhenEffectiveStatementImpl.class);

        // Check for singleton instances
        if (minElements == null && maxElements == null && mustSubstatements.isEmpty() && firstWhenStmt == null) {
            return EmptyConstraintDefinition.create(mandatory);
        }

        return new EffectiveConstraintDefinitionImpl(mandatory, minElements, maxElements,
            firstWhenStmt == null ? null : firstWhenStmt.argument(), mustSubstatements);
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
