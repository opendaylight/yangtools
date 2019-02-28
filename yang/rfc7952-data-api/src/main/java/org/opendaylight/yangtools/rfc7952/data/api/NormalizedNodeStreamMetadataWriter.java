/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.api;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;

/**
 * Extension to the NormalizedNodeStreamWriter with metadata support. Semantically this extends the event model of
 * {@link NormalizedNodeStreamWriter} with two new events:
 * <ul>
 * <li>{@link #startMetadata(int)} is within the scope of any open node and starts a block of metadata entries. It
 * is recommended to emit this block before any other events. Only
 * {@link #startMetadataEntry(QName, AnnotationSchemaNode)} and {@link NormalizedNodeStreamWriter#endNode()} are valid
 * in this node. This event may be emitted at most once for any open node.
 * </li>
 * <li>{@link #startMetadataEntry(QName, AnnotationSchemaNode)} to start a metadata entry. Its value is must be emitted
 * via {@link NormalizedNodeStreamWriter#scalarValue(Object)} before the entry is closed via
 * {@link NormalizedNodeStreamWriter#endNode()}.
 * </ul>
 *
 * <p>
 * Note that some implementations of this interface, notably those targeting streaming XML, may require metadata to
 * be emitted before any other events. Such requirement is communicated through {@link #requireMetadataFirst()} and
 * users must honor it. If such requirement is not set, metadata may be emitted at any time.
 */
@Beta
public interface NormalizedNodeStreamMetadataWriter extends NormalizedNodeStreamWriterExtension {
    /**
     * Start the metadata block for the currently-open node. Allowed events are
     * {@link #startMetadataEntry(QName, AnnotationSchemaNode)} and {@link NormalizedNodeStreamWriter#endNode()}.
     *
     * @param childSizeHint Non-negative count of expected direct child nodes or
     *                      {@link NormalizedNodeStreamWriter#UNKNOWN_SIZE} if count is unknown. This is only hint and
     *                      should not fail writing of child events, if there are more events than count.
     * @throws IllegalStateException if current node already has a metadata block or cannot receive metadata -- for
     *                               example because {@link #requireMetadataFirst()} was not honored.
     * @throws IOException if an underlying IO error occurs
     */
    void startMetadata(int childSizeHint) throws IOException;

    /**
     * Start a new metadata entry. The value of the metadata entry should be emitted through
     * {@link NormalizedNodeStreamWriter#scalarValue(Object)}.
     *
     * @param name Metadata name, as defined through {@code md:annotation}
     * @param schema Effective {@code md:annotation} schema, or null if unknown to the caller
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalStateException when this method is invoked outside of a metadata block
     * @throws IOException if an underlying IO error occurs
     */
    void startMetadataEntry(@NonNull QName name, @Nullable AnnotationSchemaNode schema) throws IOException;

    /**
     * Indicate whether metadata is required to be emitted just after an entry is open. The default implementation
     * returns false.
     *
     * @return True if metadata must occur just after the start of an entry.
     */
    default boolean requireMetadataFirst() {
        return false;
    }
}
