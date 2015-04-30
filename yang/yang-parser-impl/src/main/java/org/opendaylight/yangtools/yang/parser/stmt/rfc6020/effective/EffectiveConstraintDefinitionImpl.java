/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.HashSet;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import java.util.Collections;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

public class EffectiveConstraintDefinitionImpl implements ConstraintDefinition {
    private RevisionAwareXPath whenCondition;
    private Set<MustDefinition> mustConstraints = Collections.emptySet();
    private Boolean mandatory = false;
    private Integer minElements;
    private Integer maxElements;

    public EffectiveConstraintDefinitionImpl(EffectiveStatementBase<?, ?> parent) {

        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = parent
                .effectiveSubstatements();
        for (EffectiveStatement<?, ?> subStatement : effectiveSubstatements) {
            if (subStatement instanceof MandatoryEffectiveStatementImpl) {
                MandatoryEffectiveStatementImpl mandatoryStmt = (MandatoryEffectiveStatementImpl) subStatement;
                this.mandatory = mandatoryStmt.argument();
            }
            if (subStatement instanceof WhenEffectiveStatementImpl) {
                WhenEffectiveStatementImpl whenStmt = (WhenEffectiveStatementImpl) subStatement;
                this.whenCondition = whenStmt.argument();
            }
            if (subStatement instanceof MinElementsEffectiveStatementImpl) {
                MinElementsEffectiveStatementImpl minElementsStmt = (MinElementsEffectiveStatementImpl) subStatement;
                this.minElements = minElementsStmt.argument();
            }
            if (subStatement instanceof MaxElementsEffectiveStatementImpl) {
                MaxElementsEffectiveStatementImpl maxElementsStmt = (MaxElementsEffectiveStatementImpl) subStatement;
                String maxElementsString = maxElementsStmt.argument();
                if (maxElementsString.equals("unbounded")) {
                    this.maxElements = Integer.MAX_VALUE;
                } else {
                    this.maxElements = Integer.parseInt(maxElementsString);
                }
            }
            if (subStatement instanceof MustEffectiveStatementImpl) {
                MustEffectiveStatementImpl mustStmt = (MustEffectiveStatementImpl) subStatement;
                if (this.mustConstraints.isEmpty()) {
                    this.mustConstraints = new HashSet<>();
                }
                this.mustConstraints.add(mustStmt);
            }
        }

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
