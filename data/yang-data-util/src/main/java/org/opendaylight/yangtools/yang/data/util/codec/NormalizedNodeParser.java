/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * Simple interface to parsing of documents containing YANG-modeled data. This interface is modeled on what typical
 * implementations of <a href="https://www.rfc-editor.org/rfc/rfc8040">RESTCONF</a> require.
 */
@Beta
@NonNullByDefault
public interface NormalizedNodeParser {
    /**
     * A DTO capturing the result of
     * {@link NormalizedNodeParser#parseChildData(EffectiveStatementInference, InputStream)}.
     *
     * @param pathSuffix {@link YangInstanceIdentifier} steps that need to be concatenated to the request path to form
     *                   a {@link YangInstanceIdentifier} pointing to the returned data
     * @param data parsed data
     */
    record SuffixAndData(List<PathArgument> pathSuffix, NormalizedNode data) {
        public SuffixAndData {
            if (pathSuffix.isEmpty()) {
                throw new IllegalArgumentException("Path suffix must not be empty");
            }
            final var dataName = data.name();
            if (!dataName.equals(pathSuffix.get(pathSuffix.size() - 1))) {
                throw new IllegalArgumentException("Last item in " + pathSuffix + " does not match data " + dataName);
            }
        }
    }

    /**
     * Parse the contents of an {@link InputStream} as the contents of a data store.
     *
     * @param containerName {@link NodeIdentifier} corresponding to the root data store resource
     * @param stream the {@link InputStream} to parse
     * @return parsed {@link ContainerNode} corresponding to the data store root
     * @throws NullPointerException if any argument is {@code null}
     * @throws IOException if an error occurs
     */
    ContainerNode parseDatastore(NodeIdentifier containerName, InputStream stream) throws IOException;

    /**
     * Parse the contents of an {@link InputStream} as the data resource.
     *
     * @param inference pointer to the data resource
     * @param stream the {@link InputStream} to parse
     * @return Parsed {@link ContainerNode} corresponding to the data store root
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code inference} does not to point to a resource
     * @throws IOException if an error occurs
     */
    NormalizedNode parseData(EffectiveStatementInference inference, InputStream stream) throws IOException;

    SuffixAndData parseChildData(EffectiveStatementInference inference, InputStream stream) throws IOException;

    ContainerNode parseInput(EffectiveStatementInference inference, InputStream stream) throws IOException;

    ContainerNode parseOutput(EffectiveStatementInference inference, InputStream stream) throws IOException;
}
