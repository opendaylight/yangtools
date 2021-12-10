/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

/**
 * A mouthful of an exception, reported when the requested schema node does exist in the schema tree, but is filtered
 * from the current data tree. This can occur when a {@code config=false} node is referenced in the operational data
 * tree.
 */
final class ExcludedDataSchemaNodeException extends Exception {
    private static final long serialVersionUID = 1L;

    ExcludedDataSchemaNodeException(final String message) {
        super(message);
    }
}
