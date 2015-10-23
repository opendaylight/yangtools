/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;

public final class ConstraintsBuilderImpl implements ConstraintsBuilder {
    private static final ConstraintDefinitionImpl EMPTY_CONSTRAINT = new ConstraintDefinitionImpl();
    private static final ConstraintDefinitionImpl EMPTY_MANDATORY_CONSTRAINT;

    static {
        ConstraintDefinitionImpl c = new ConstraintDefinitionImpl();
        c.setMandatory(true);

        EMPTY_MANDATORY_CONSTRAINT = c;
    }

    private final String moduleName;
    private final int line;
    private final Set<MustDefinition> mustDefinitions;
    private ConstraintDefinitionImpl instance;
    private RevisionAwareXPath whenStmt;
    private String whenCondition;
    private boolean mandatory;
    private Integer min;
    private Integer max;

    public ConstraintsBuilderImpl(final String moduleName, final int line) {
        this.moduleName = moduleName;
        this.line = line;
        mustDefinitions = new HashSet<>();
    }

    ConstraintsBuilderImpl(final ConstraintsBuilder b) {
        this.moduleName = b.getModuleName();
        this.line = b.getLine();
        mustDefinitions = new HashSet<>(b.getMustDefinitions());
        whenCondition = b.getWhenCondition();
        mandatory = b.isMandatory();
        min = b.getMinElements();
        max = b.getMaxElements();
    }

    ConstraintsBuilderImpl(final String moduleName, final int line, final ConstraintDefinition base) {
        this.moduleName = moduleName;
        this.line = line;
        whenStmt = base.getWhenCondition();
        mustDefinitions = new HashSet<>(base.getMustConstraints());
        mandatory = base.isMandatory();
        min = base.getMinElements();
        max = base.getMaxElements();
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#build()
     */
    @Override
    public ConstraintDefinition build() {
        if (instance != null) {
            return instance;
        }

        if (whenStmt == null) {
            if (whenCondition == null) {
                whenStmt = null;
            } else {
                whenStmt = new RevisionAwareXPathImpl(whenCondition, false);
            }
        }

        ConstraintDefinitionImpl newInstance = new ConstraintDefinitionImpl();
        newInstance.setWhenCondition(whenStmt);
        newInstance.setMandatory(mandatory);
        newInstance.setMinElements(min);
        newInstance.setMaxElements(max);

        if (!mustDefinitions.isEmpty()) {
            newInstance.setMustConstraints(mustDefinitions);
        }
        if (EMPTY_CONSTRAINT.equals(newInstance)) {
            newInstance = EMPTY_CONSTRAINT;
        } else if (EMPTY_MANDATORY_CONSTRAINT.equals(newInstance)) {
            newInstance = EMPTY_MANDATORY_CONSTRAINT;
        }

        instance = newInstance;
        return instance;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#getModuleName()
     */
    @Override
    public String getModuleName() {
        return moduleName;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#getLine()
     */
    @Override
    public int getLine() {
        return line;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#getMinElements()
     */
    @Override
    public Integer getMinElements() {
        return min;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#setMinElements(java.lang.Integer)
     */
    @Override
    public void setMinElements(final Integer minElements) {
        this.min = minElements;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#getMaxElements()
     */
    @Override
    public Integer getMaxElements() {
        return max;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#setMaxElements(java.lang.Integer)
     */
    @Override
    public void setMaxElements(final Integer maxElements) {
        this.max = maxElements;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#getMustDefinitions()
     */
    @Override
    public Set<MustDefinition> getMustDefinitions() {
        return mustDefinitions;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#addMustDefinition(org.opendaylight.yangtools.yang.model.api.MustDefinition)
     */
    @Override
    public void addMustDefinition(final MustDefinition must) {
        mustDefinitions.add(must);
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#getWhenCondition()
     */
    @Override
    public String getWhenCondition() {
        return whenCondition;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#addWhenCondition(java.lang.String)
     */
    @Override
    public void addWhenCondition(final String whenCondition) {
        this.whenCondition = whenCondition;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#isMandatory()
     */
    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IConstraintsBuilder#setMandatory(boolean)
     */
    @Override
    public void setMandatory(final boolean mandatory) {
        this.mandatory = mandatory;
    }

    private static final class ConstraintDefinitionImpl implements ConstraintDefinition {
        private RevisionAwareXPath whenCondition;
        private Set<MustDefinition> mustConstraints = Collections.emptySet();
        private Boolean mandatory = false;
        private Integer minElements;
        private Integer maxElements;

        @Override
        public RevisionAwareXPath getWhenCondition() {
            return whenCondition;
        }

        private void setWhenCondition(final RevisionAwareXPath whenCondition) {
            this.whenCondition = whenCondition;
        }

        @Override
        public Set<MustDefinition> getMustConstraints() {
            return mustConstraints;
        }

        private void setMustConstraints(final Set<MustDefinition> mustConstraints) {
            if (mustConstraints != null) {
                this.mustConstraints = ImmutableSet.copyOf(mustConstraints);
            }
        }

        @Override
        public boolean isMandatory() {
            return mandatory;
        }

        private void setMandatory(final boolean mandatory) {
            this.mandatory = mandatory;
        }

        @Override
        public Integer getMinElements() {
            return minElements;
        }

        private void setMinElements(final Integer minElements) {
            this.minElements = minElements;
            if (minElements != null && minElements > 0) {
                mandatory = true;
            }
        }

        @Override
        public Integer getMaxElements() {
            return maxElements;
        }

        private void setMaxElements(final Integer maxElements) {
            this.maxElements = maxElements;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(whenCondition);
            result = prime * result + Objects.hashCode(mustConstraints);
            result = prime * result + Objects.hashCode(minElements);
            result = prime * result + Objects.hashCode(maxElements);
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
            ConstraintDefinitionImpl other = (ConstraintDefinitionImpl) obj;
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
            StringBuilder sb = new StringBuilder(ConstraintDefinitionImpl.class.getSimpleName());
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

    /*
     * @deprecated Use #build() instead.
     */
    @Override
    @Deprecated
    public ConstraintDefinition toInstance() {
        return build();
    }

}
