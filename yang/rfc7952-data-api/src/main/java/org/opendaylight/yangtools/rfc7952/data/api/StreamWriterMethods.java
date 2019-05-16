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

/**
 * Common streaming methods. Used to share definitions between
 * {@link NormalizedMetadataStreamWriter#metadata(ImmutableMap)} and
 * {@link OpaqueAnydataStreamWriter#metadata(ImmutableMap)}.
 */
@Beta
public interface StreamWriterMethods {
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
