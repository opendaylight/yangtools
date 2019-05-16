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
public interface NormalizedMetadataStreamWriter extends NormalizedNodeStreamWriterExtension, StreamWriterMethods {

}
