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
import org.opendaylight.yangtools.yang.model.api.*;

/**
 * Mock Leaf Schema Node designated to increase branch coverage in test cases.
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class TestLeafSchemaNode implements LeafSchemaNode {
    @Override public TypeDefinition<?> getType() {
        return null;
    }

    @Override public String getDefault() {
        return null;
    }

    @Override public String getUnits() {
        return null;
    }

    @Override public boolean isAugmenting() {
        return false;
    }

    @Override public boolean isAddedByUses() {
        return false;
    }

    @Override public boolean isConfiguration() {
        return false;
    }

    @Override public ConstraintDefinition getConstraints() {
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
