/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Extension to the NormalizedNodeStreamWriter with metadata support. Semantically this extends the event model of
 * {@link NormalizedNodeStreamWriter} with two new events:
 * <ul>
 * <li>{@link #startMetadata(int)} is within the scope of any open node and starts a block of metadata entries. It
 * is recommended to emit this block before any other events. Only {@link #startMetadataEntry(QName)} and
 * {@link #endNode()} are valid in this node.
 * </li>
 * <li>{@link #startMetadataEntry(QName)} to start a metadata entry. Its value is must be emitted via
 * {@link #scalarValue(Object)} before the entry is closed via {@link #endNode()}.
 * </ul>
 */
public interface NormalizedNodeStreamMetadataWriter extends NormalizedNodeStreamWriter {

    void startMetadata(int childSizeHint) throws IOException;

    default void nextAnnotationSchemaNode(final AnnotationSchemaNode schema) {
        requireNonNull(schema);
    }

    void startMetadataEntry(QName name) throws IOException;
}
