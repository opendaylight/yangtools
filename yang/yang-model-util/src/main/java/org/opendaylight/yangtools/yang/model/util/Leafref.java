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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

/**
 * The <code>default</code> implementation of Instance Leafref Type Definition
 * interface.
 *
 * @see LeafrefTypeDefinition
 */
public final class Leafref implements LeafrefTypeDefinition {
    private static final QName NAME = BaseTypes.constructQName("leafref");
    private static final SchemaPath PATH = BaseTypes.schemaPath(NAME);
    private static final String DESCRIPTION = "The leafref type is used to reference a particular leaf instance in the data tree.";
    private static final String REF = "https://tools.ietf.org/html/rfc6020#section-9.9";

    private final RevisionAwareXPath xpath;
    private final DataSchemaNode targetNode;

    public Leafref(final RevisionAwareXPath xpath, final DataSchemaNode targetNode) {
        this.xpath = xpath;
        this.targetNode = targetNode;
    }

    @Override
    public LeafrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return "";
    }

    @Override
    public Object getDefaultValue() {
        return this;
    }

    @Override
    public QName getQName() {
        return NAME;
    }

    @Override
    public SchemaPath getPath() {
        return PATH;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getReference() {
        return REF;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return xpath;
    }

    @Override
    public DataSchemaNode getTargetNode() {
        return targetNode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        Leafref other = (Leafref) obj;
        if (xpath == null) {
            if (other.xpath != null) {
                return false;
            }
        } else if (!xpath.equals(other.xpath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("type ");
        builder.append(NAME);
        builder.append(" [xpath=");
        builder.append(xpath);
        builder.append("]");
        return builder.toString();
    }
}
