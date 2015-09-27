/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.builder.api.RefineBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractBuilder;

public final class RefineHolderImpl extends AbstractBuilder implements RefineBuilder {
    private final String targetPathString;
    private String defaultStr;
    private String description;
    private String reference;
    private Boolean config;
    private Boolean mandatory;
    private Boolean presence;
    private MustDefinition must;
    private Integer minElements;
    private Integer maxElements;

    public RefineHolderImpl(final String moduleName, final int line, final String name) {
        super(moduleName, line);
        this.targetPathString = name;
    }

    @Override
    public String getDefaultStr() {
        return defaultStr;
    }

    public void setDefaultStr(final String defaultStr) {
        this.defaultStr = defaultStr;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#getReference()
     */
    @Override
    public String getReference() {
        return reference;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setReference(java.lang.String)
     */
    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#isConfiguration()
     */
    @Override
    public Boolean isConfiguration() {
        return config;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setConfiguration(java.lang.Boolean)
     */
    @Override
    public void setConfiguration(final Boolean config) {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#isMandatory()
     */
    @Override
    public Boolean isMandatory() {
        return mandatory;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setMandatory(java.lang.Boolean)
     */
    @Override
    public void setMandatory(final Boolean mandatory) {
        this.mandatory = mandatory;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#isPresence()
     */
    @Override
    public Boolean isPresence() {
        return presence;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setPresence(java.lang.Boolean)
     */
    @Override
    public void setPresence(final Boolean presence) {
        this.presence = presence;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#getMust()
     */
    @Override
    public MustDefinition getMust() {
        return must;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setMust(org.opendaylight.yangtools.yang.model.api.MustDefinition)
     */
    @Override
    public void setMust(final MustDefinition must) {
        this.must = must;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#getMinElements()
     */
    @Override
    public Integer getMinElements() {
        return minElements;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setMinElements(java.lang.Integer)
     */
    @Override
    public void setMinElements(final Integer minElements) {
        this.minElements = minElements;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#getMaxElements()
     */
    @Override
    public Integer getMaxElements() {
        return maxElements;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IRefineBuilder#setMaxElements(java.lang.Integer)
     */
    @Override
    public void setMaxElements(final Integer maxElements) {
        this.maxElements = maxElements;
    }

    @Override
    public String toString() {
        return "refine " + targetPathString;
    }

    @Override
    public Status getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStatus(final Status status) {
        // TODO Auto-generated method stub

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(addedUnknownNodes);
        result = prime * result + Objects.hashCode(config);
        result = prime * result + Objects.hashCode(defaultStr);
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(mandatory);
        result = prime * result + Objects.hashCode(maxElements);
        result = prime * result + Objects.hashCode(minElements);
        result = prime * result + Objects.hashCode(must);
        result = prime * result + Objects.hashCode(targetPathString);
        result = prime * result + Objects.hashCode(getParent());
        result = prime * result + Objects.hashCode(presence);
        result = prime * result + Objects.hashCode(reference);
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
        RefineHolderImpl other = (RefineHolderImpl) obj;
        if (addedUnknownNodes == null) {
            if (other.addedUnknownNodes != null) {
                return false;
            }
        } else if (!addedUnknownNodes.equals(other.addedUnknownNodes)) {
            return false;
        }
        if (config == null) {
            if (other.config != null) {
                return false;
            }
        } else if (!config.equals(other.config)) {
            return false;
        }
        if (defaultStr == null) {
            if (other.defaultStr != null) {
                return false;
            }
        } else if (!defaultStr.equals(other.defaultStr)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (mandatory == null) {
            if (other.mandatory != null) {
                return false;
            }
        } else if (!mandatory.equals(other.mandatory)) {
            return false;
        }
        if (maxElements == null) {
            if (other.maxElements != null) {
                return false;
            }
        } else if (!maxElements.equals(other.maxElements)) {
            return false;
        }
        if (minElements == null) {
            if (other.minElements != null) {
                return false;
            }
        } else if (!minElements.equals(other.minElements)) {
            return false;
        }
        if (must == null) {
            if (other.must != null) {
                return false;
            }
        } else if (!must.equals(other.must)) {
            return false;
        }
        if (targetPathString == null) {
            if (other.targetPathString != null) {
                return false;
            }
        } else if (!targetPathString.equals(other.targetPathString)) {
            return false;
        }
        if (getParent() == null) {
            if (other.getParent() != null) {
                return false;
            }
        } else if (!getParent().equals(other.getParent())) {
            return false;
        }
        if (presence == null) {
            if (other.presence != null) {
                return false;
            }
        } else if (!presence.equals(other.presence)) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        return true;
    }



    @Override
    public Object build() {
        // FIXME: Currently RefineBuilder.build() is not used
        // build should returned refined element, so
        // whole refine process is encapsulated in this refinement
        // statement.
        return null;
    }

    @Override
    public String getTargetPathString() {
        return targetPathString;
    }

}
