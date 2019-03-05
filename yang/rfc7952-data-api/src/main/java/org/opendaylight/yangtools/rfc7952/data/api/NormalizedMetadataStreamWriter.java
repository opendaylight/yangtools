/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;

/**
 * Extension to the NormalizedNodeStreamWriter with metadata support. Semantically this extends the event model of
 * {@link NormalizedNodeStreamWriter} with a new event, {@link #metadata(ImmutableMap)}. This event is valid on any
 * open node. This event may be emitted only once.
 *
 * <p>
 * Note that some implementations of this interface, notably those targeting streaming XML, may require metadata to
 * be emitted before any other events. Such requirement is communicated through {@link #requireMetadataFirst()} and
 * users must honor it. If such requirement is not set, metadata may be emitted at any time.
 *
 * <p>
 * Furthermore implementations targeting RFC7952 encoding towards external systems are required to handle metadata
 * attached to {@code leaf-list} and {@code list} nodes by correctly extending them to each entry.
 */
@Beta
public interface NormalizedMetadataStreamWriter extends NormalizedNodeStreamWriterExtension {
    /**
     * Emit a block of metadata associated with the currently-open node. The argument is a map of annotation names,
     * as defined {@code md:annotation} extension. Values are normalized objects, which are required to be
     * effectively-immutable.
     *
     * @param metadata Metadata block
     * @throws NullPointerException if {@code metadata} is null
     * @throws IllegalStateException when this method is invoked outside of an open node or metadata has already been
     *                               emitted.
     * @throws IOException if an underlying IO error occurs
     */
    void metadata(ImmutableMap<QName, Object> metadata) throws IOException;

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
