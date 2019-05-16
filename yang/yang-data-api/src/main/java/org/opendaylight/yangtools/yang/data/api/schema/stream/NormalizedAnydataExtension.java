/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydataNode;

/**
 * A {@link NormalizedNodeStreamWriterExtension} supporting streaming of normalized {@code anydata} nodes.
 */
@Beta
public interface NormalizedAnydataExtension extends NormalizedNodeStreamWriterExtension {
    /**
     * Start emitting a new anydata node identified by name. The content of the node should be set via the returned
     * {@link NormalizedNodeStreamWriter}.
     *
     * @param name The name of the anydata element
     * @return A {@link NormalizedNodeStreamWriter} which handles the node's interior events
     * @throws IOException if an underlying IO error occurs
     */
    @NonNull NormalizedNodeStreamWriter startNormalizedAnydataNode(NodeIdentifier name) throws IOException;

    // FIXME: 5.0.0: the requirement for ordering should come from the writer
    default void streamNormalizedAnydataNode(final NormalizedAnydataNode node, final boolean orderKeyLeaves)
            throws IOException {
        NormalizedNodeWriter.forStreamWriter(startNormalizedAnydataNode(node.getIdentifier()), orderKeyLeaves)
            .write(node.getValue())
            .flush();
    }

    default void streamNormalizedAnydataNode(final NormalizedAnydataNode node) throws IOException {
        streamNormalizedAnydataNode(node, false);
    }
}
