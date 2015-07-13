/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types.stmt.parser.retest;

import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

/**
 * Mock LeafrefDypeDefinition implementation with RevisionAwareXPath null reference
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class LeafrefTypeWithNullXpath implements LeafrefTypeDefinition {
    @Override public RevisionAwareXPath getPathStatement() {
        return null;
    }

    @Override public LeafrefTypeDefinition getBaseType() {
        return null;
    }

    @Override public String getUnits() {
        return null;
    }

    @Override public Object getDefaultValue() {
        return null;
    }

    @Override public QName getQName() {
        return null;
    }

    @Override public SchemaPath getPath() {
        return null;
    }

    @Override public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return null;
    }

    @Override public String getDescription() {
        return null;
    }

    @Override public String getReference() {
        return null;
    }

    @Override public Status getStatus() {
        return null;
    }
}
