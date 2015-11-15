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
import org.opendaylight.yangtools.concepts.Immutable;
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
 * Instance Identifier has only two possible variants - one with
 * {@link #requireInstance()} which returns true, other one
 * returns false.
 *
 * @see InstanceIdentifierTypeDefinition
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#instanceIdentifierType())} instead
 */
@Deprecated
public final class InstanceIdentifierType implements InstanceIdentifierTypeDefinition, Immutable {

    private static final QName NAME = BaseTypes.INSTANCE_IDENTIFIER_QNAME;
    private static final SchemaPath PATH = SchemaPath.create(true, NAME);
    private static final String DESCRIPTION = "The instance-identifier built-in type is used to "
            + "uniquely identify a particular instance node in the data tree.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.13";
    private static final String UNITS = "";

    private static final InstanceIdentifierType INSTANCE_WITH_REQUIRED_TRUE = new InstanceIdentifierType(true);
    private static final InstanceIdentifierType INSTANCE_WITH_REQUIRED_FALSE = new InstanceIdentifierType(false);

    private final Boolean requireInstance;

    private InstanceIdentifierType(final boolean requiredInstance) {
        this.requireInstance = requiredInstance;
    }

    public static InstanceIdentifierType getInstance() {
        return INSTANCE_WITH_REQUIRED_TRUE;
    }

    public static InstanceIdentifierType create(final boolean requireInstance) {
        return requireInstance ? INSTANCE_WITH_REQUIRED_TRUE : INSTANCE_WITH_REQUIRED_FALSE;
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
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return null;
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
     * org.opendaylight.yangtools.yang.model.api.SchemaNode#getExtensionSchemaNodes()
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
    @Deprecated
    public RevisionAwareXPath getPathStatement() {
        return null;
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
        result = prime * result + requireInstance.hashCode();
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
        InstanceIdentifierType other = (InstanceIdentifierType) obj;
        return requireInstance.equals(other.requireInstance);
    }
}
