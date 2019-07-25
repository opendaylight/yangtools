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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A {@link NormalizedNodeStreamWriterExtension} supporting streaming of {@code anydata} nodes.
 *
 * @author Robert Varga
 */
@Beta
// FIXME: 4.0.0: integrate this into NormalizedNodeStreamWriter
public interface AnydataExtension extends NormalizedNodeStreamWriterExtension {
    /**
     * Start emitting a new anydata node identified by name.
     *
     * @param name The name of the anydata element
     * @param objectModel The object model of anydata content
     * @return True if the specified object model is supported by this extension and the process of emitting the node
     *         has started. False if the object model is not supported and the node has not started to be emitted.
     * @throws IOException if an underlying IO error occurs
     */
    boolean startAnydataNode(NodeIdentifier name, Class<?> objectModel) throws IOException;
}
