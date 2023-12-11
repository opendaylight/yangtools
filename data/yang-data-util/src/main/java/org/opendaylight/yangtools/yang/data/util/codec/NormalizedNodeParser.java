/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;
import java.io.InputStream;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Qualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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
            pathSuffix = List.copyOf(pathSuffix);
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
     * Parse the contents of an {@link InputStream} as the contents of a data store. This method's signature is a bit
     * counter-intuitive. {@code rootNamespace} and {@code rootName} collectively encode the expected root element,
     * which may not be expressed in the underlying YANG data model.
     *
     * <p>
     * The reason for this is that YANG does not define an explicit name of the datastore root resource, but protocol
     * encodings require this conceptual root to be encapsulated in protocol documents and the approaches taken differ
     * from protocol to protocol. NETCONF operates in terms of YANG-modeled RPC operations, where this conceptual root
     * is given an anchor -- {@code get-config} output's {@code anyxml data}. RESTCONF operates in terms of HTTP
     * payloads and while it models such an anchor, it is rather unnatural {@code container restconf} with description
     * defining its magic properties and it is not feasible for YANG parser to help us with that.
     *
     * <p>
     * Therefore this method takes the name of the root element in two arguments, which together define its value in
     * both JSON-based (module + localName} and XML-based (namespace + localName) encodings. Implementations of this
     * method are expected to use this information and treat the root element outside of their usual YANG-informed
     * processing.
     *
     * <p>
     * For example, XML parsers will pick {@code rootNamespace} to match the root element's namespace and
     * {@code rootName.getLocalName()} to match the element's local name. JSON parsers, on the other hand, will
     * disregard {@code rootNamespace} and just use {@code rootName.getPrefix()} and {@code rootName.getLocalName()} to
     * match the top-level JSON object's sole named member.
     *
     * @param rootNamespace the expected XML namespace of the root element
     * @param rootName the expected name of the root element
     * @param stream the {@link InputStream} to parse
     * @return parsed {@link ContainerNode} corresponding to the data store root
     * @throws NullPointerException if any argument is {@code null}
     * @throws NormalizedNodeParserException if an error occurs
     */
    ContainerNode parseDatastore(XMLNamespace rootNamespace, Qualified rootName, InputStream stream)
        throws NormalizedNodeParserException;

    /**
     * Parse the contents of an {@link InputStream} as the data resource.
     *
     * @param inference pointer to the data resource
     * @param stream the {@link InputStream} to parse
     * @return Parsed {@link ContainerNode} corresponding to the data store root
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code inference} does not to point to a resource
     * @throws NormalizedNodeParserException if an error occurs
     */
    NormalizedNode parseData(EffectiveStatementInference inference, InputStream stream)
        throws NormalizedNodeParserException;

    SuffixAndData parseChildData(EffectiveStatementInference parentInference, InputStream stream)
        throws NormalizedNodeParserException;

    ContainerNode parseInput(EffectiveStatementInference operationInference, InputStream stream)
        throws NormalizedNodeParserException;

    ContainerNode parseOutput(EffectiveStatementInference operationInference, InputStream stream)
        throws NormalizedNodeParserException;
}
