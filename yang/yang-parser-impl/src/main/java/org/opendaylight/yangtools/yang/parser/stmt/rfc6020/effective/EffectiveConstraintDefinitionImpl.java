/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

public class EffectiveConstraintDefinitionImpl implements ConstraintDefinition {
    private final RevisionAwareXPath whenCondition;
    private final Set<MustDefinition> mustConstraints;
    private final Boolean mandatory;
    private final Integer minElements;
    private final Integer maxElements;

    public EffectiveConstraintDefinitionImpl(EffectiveStatementBase<?, ?> parent) {

        MandatoryEffectiveStatementImpl firstMandatoryStmt = parent
                .firstEffective(MandatoryEffectiveStatementImpl.class);
        this.mandatory = (firstMandatoryStmt == null) ? false
                : firstMandatoryStmt.argument();

        WhenEffectiveStatementImpl firstWhenStmt = parent
                .firstEffective(WhenEffectiveStatementImpl.class);
        this.whenCondition = (firstWhenStmt == null) ? null : firstWhenStmt
                .argument();

        MinElementsEffectiveStatementImpl firstMinElementsStmt = parent
                .firstEffective(MinElementsEffectiveStatementImpl.class);
        this.minElements = (firstMinElementsStmt == null) ? 0
                : firstMinElementsStmt.argument();

        MaxElementsEffectiveStatementImpl firstMaxElementsStmt = parent
                .firstEffective(MaxElementsEffectiveStatementImpl.class);
        String maxElementsArg = (firstMaxElementsStmt == null) ? "unbounded"
                : firstMaxElementsStmt.argument();
        if (maxElementsArg.equals("unbounded")) {
            this.maxElements = Integer.MAX_VALUE;
        } else {
            this.maxElements = Integer.valueOf(maxElementsArg);
        }

        Collection<MustDefinition> mustSubstatements = parent
                .allSubstatementsOfType(MustDefinition.class);
        this.mustConstraints = ImmutableSet.copyOf(mustSubstatements);
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
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((whenCondition == null) ? 0 : whenCondition.hashCode());
        result = prime * result
                + ((mustConstraints == null) ? 0 : mustConstraints.hashCode());
        result = prime * result
                + ((minElements == null) ? 0 : minElements.hashCode());
        result = prime * result
                + ((maxElements == null) ? 0 : maxElements.hashCode());
        result = prime * result + mandatory.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EffectiveConstraintDefinitionImpl other = (EffectiveConstraintDefinitionImpl) obj;
        if (whenCondition == null) {
            if (other.whenCondition != null) {
                return false;
            }
        } else if (!whenCondition.equals(other.whenCondition)) {
            return false;
        }
        if (mustConstraints == null) {
            if (other.mustConstraints != null) {
                return false;
            }
        } else if (!mustConstraints.equals(other.mustConstraints)) {
            return false;
        }
        if (!mandatory.equals(other.mandatory)) {
            return false;
        }
        if (minElements == null) {
            if (other.minElements != null) {
                return false;
            }
        } else if (!minElements.equals(other.minElements)) {
            return false;
        }
        if (maxElements == null) {
            if (other.maxElements != null) {
                return false;
            }
        } else if (!maxElements.equals(other.maxElements)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                EffectiveConstraintDefinitionImpl.class.getSimpleName());
        sb.append("[");
        sb.append("whenCondition=").append(whenCondition);
        sb.append(", mustConstraints=").append(mustConstraints);
        sb.append(", mandatory=").append(mandatory);
        sb.append(", minElements=").append(minElements);
        sb.append(", maxElements=").append(maxElements);
        sb.append("]");
        return sb.toString();
    }
}
