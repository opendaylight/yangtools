/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

/**
 * An implementation of apply operation which fails to do anything, consistently. An instance of this class is used by
 * the data tree if it does not have a SchemaContext attached and hence cannot perform anything meaningful.
 */
final class AlwaysFailOperation extends FullyDelegatedModificationApplyOperation {
    static final ModificationApplyOperation INSTANCE = new AlwaysFailOperation();

    private AlwaysFailOperation() {

    }

    @Override
    ModificationApplyOperation delegate() {
        throw new IllegalStateException("Schema Context is not available.");
    }
}
