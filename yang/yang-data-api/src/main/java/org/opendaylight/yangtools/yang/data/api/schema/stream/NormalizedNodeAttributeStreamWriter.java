/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Extension to the {@link NormalizedNodeStreamWriter}, providing additional APIs
 * enabling users to emit attributes for Normalized Nodes
 *
 * @see NormalizedNodeStreamWriter
 */
public interface NormalizedNodeAttributeStreamWriter extends NormalizedNodeStreamWriter, NormalizedNodeStreamAttributeWriter {

    /**
     * Emit a collection of attributes for a normalized node being currently written.
     *
     * @param attributes map of attributes to be emitted onto a node.
     *                   QName(map key) provides the namespace and the key for the attribute.
     *                   String value(map value) provides the value for the attribute.
     *
     * @throws IllegalStateException If the writer is currently in a state where the attributes cannot be emitted.
     *                               This covers situations when a node was not yet started or started node is not allowed to have attributes in the output format
     *                               (for xml this covers: <code>map</code>, <code>choice</code>, <code>unkeyed list</code> and <code>augment</code>).
     * @throws IllegalArgumentException If any of the values contains illegal characters for the output format.
     * @throws IOException If an underlying IO error occurs
     */
    void emitAttributes(@Nonnull final Map<QName, String> attributes) throws IOException;

    // TODO move the methods from NormalizedNodeStreamAttributeWriter, document them, migrate the users and remove the old interface for backwards compatibility and mark them deprecated

}
