/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

/**
 * The <code>default</code> implementation of Instance Identifier Type
 * Definition interface.
 *
 * @see InstanceIdentifierTypeDefinition
 */
public final class InstanceIdentifier implements InstanceIdentifierTypeDefinition {
    private static final QName NAME = BaseTypes.constructQName("instance-identifier");
    private static final SchemaPath PATH = new SchemaPath(Collections.singletonList(NAME), true);
    private static final String DESCRIPTION = "The instance-identifier built-in type is used to "
            + "uniquely identify a particular instance node in the data tree.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.13";

    private final RevisionAwareXPath xpath;
    private static final String UNITS = "";
    private boolean requireInstance = true;

    private static final int HASH_BOOLEAN_TRUE = 1231;
    private static final int HASH_BOOLEAN_FALSE = 1237;

    public InstanceIdentifier(final RevisionAwareXPath xpath) {
        this.xpath = xpath;
    }

    public InstanceIdentifier(final RevisionAwareXPath xpath, final boolean requireInstance) {
        this.xpath = xpath;
        this.requireInstance = requireInstance;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getBaseType()
     */
    @Override
    public InstanceIdentifierTypeDefinition getBaseType() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.TypeDefinition#getUnits()
     */
    @Override
    public String getUnits() {
        return UNITS;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getDefaultValue
     * ()
     */
    @Override
    public Object getDefaultValue() {
        return xpath;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getQName()
     */
    @Override
    public QName getQName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getPath()
     */
    @Override
    public SchemaPath getPath() {
        return PATH;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.SchemaNode#getDescription()
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getReference()
     */
    @Override
    public String getReference() {
        return REFERENCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getStatus()
     */
    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.SchemaNode#getExtensionSchemaNodes
     * ()
     */
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.type.
     * InstanceIdentifierTypeDefinition# getPathStatement()
     */
    @Override
    public RevisionAwareXPath getPathStatement() {
        return xpath;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.type.
     * InstanceIdentifierTypeDefinition# requireInstance()
     */
    @Override
    public boolean requireInstance() {
        return requireInstance;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (requireInstance ? HASH_BOOLEAN_TRUE : HASH_BOOLEAN_FALSE);
        result = prime * result + ((xpath == null) ? 0 : xpath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InstanceIdentifier other = (InstanceIdentifier) obj;
        if (requireInstance != other.requireInstance) {
            return false;
        }
        if (xpath == null) {
            if (other.xpath != null) {
                return false;
            }
        } else if (!xpath.equals(other.xpath)) {
            return false;
        }
        return true;
    }

}
