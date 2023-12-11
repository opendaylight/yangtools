/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * Interface to parsing of {@link InputStream}s containing YANG-modeled data. While the design of this interface is
 * guided by what a typical implementation of a <a href="https://www.rfc-editor.org/rfc/rfc8040">RESTCONF</a> server or
 * client might require, and it is not limited solely to that use case and should be used wherever its methods provide
 * the required semantics.
 *
 * <p>
 * The core assumption is that the user knows the general context in which a particular document, provided as an
 * {@link InputStream}, needs to be interpreted.
 *
 * <p>
 * In RESTCONF that context is provided by the HTTP request method and the HTTP request URI. On the server side these
 * expect to be differentiated between requests to
 * <ul>
 *   <li>invoke an {@code rpc} or an {@code action}, catered to by
 *       {@link #parseInput(EffectiveStatementInference, InputStream)}</li>
 *   <li>replace the contents of a particular data store, catered to by
 *       {@link #parseDatastore(QName, Unqualified, InputStream)}<li>
 *   <li>create, replace or otherwise modify a directly identified data store resource, catered to by
 *       {@link #parseData(EffectiveStatementInference, InputStream)}</li>
 *   <li>create an indirectly identified data store resource, catered to by
 *       {@link #parseChildData(EffectiveStatementInference, InputStream)}</li>
 * </ul>
 * On the client side, these are similarly differentiated between responses to
 * <ul>
 *   <li>invoke an {@code rpc} or an {@code action}, catered to by
 *       {@link #parseOutput(EffectiveStatementInference, InputStream)}</li>
 *   <li>replace the contents of a particular data store, catered to by
 *       {@link #parseDatastore(QName, Unqualified, InputStream)}<li>
 *   <li>create, replace or otherwise modify a directly identified data store resource, catered to by
 *       {@link #parseData(EffectiveStatementInference, InputStream)}</li>
 * </ul>
 */
@NonNullByDefault
public interface InputStreamNormalizer {
    /*
     * API design notes
     *
     * This interface uses EffectiveStatementInference in places where YangInstanceIdentifier might be convenient. This
     * is on purpose, as we want to provide an interface between standards-based yang-model-api and provide enough rope
     * for integration with YangInstanceIdentifier, but not require users to necessarily use it.
     *
     * The reason for that is that an empty YangInstanceIdentifier is not really a YANG construct, but rather something
     * yang-data-tree-api (mis)uses.
     *
     * Futhermore we do not want to force users to provide a YangInstanceIdentifier for efficiency reasons. In the case
     * of RESTCONF, which is guiding the design here, the caller would acquire a YangInstanceIdentifier through parsing
     * the request URL. That means the caller was dealing with yang-model-api and therefore have likely seen
     * a SchemaInferenceStack corresponding to that identifier and can take a snapshot in the form or an
     * EffectiveStatementInference. This has the added benefit of keeping semantics clear: we expect inferences to be
     * the result of YANG-defined processing without introducing the additional friction of having to deal with the
     * differences in data tree addressing. Again, we provide enough rope to do bridge that gap easily if the user needs
     * to do so.
     *
     * Another case for not exposing YangInstanceIdentifier-based methods is that implementations of this interface are
     * expected to be bound to an EffectiveModelContext, but we do not want to expose that via this this interface
     * extending EffectiveModelContextProvider -- at the end of the day implementations may provide the required
     * functionality through hard-coding against some concrete set of of YANG models.
     *
     * PrefixAndData is using an explicit List<PathArgument> instead of a relative YangInstanceIdentifier in order to
     * make a clear distinction of use: the prefix is meant to be interpreted and must not be confused with something
     * that can, for example, be stored as a 'type instance-identifier' value or a DataTreeSnapshot.readNode() argument.
     *
     * Similar reasoning goes for the use of EffectiveStatementInference: it is a generalised concept, which could be
     * to reduce the number of methods in this interface, each method places explicit requirements on what an acceptable
     * EffectiveStatementInference argument looks like. This is done on purpose, so that we bind to explicit semantics
     * of that particular method, e.g. being explicit about semantics of a method rather than overloading methods with
     * multiple semantic modes.
     */

    /**
     * A DTO capturing the result of
     * {@link InputStreamNormalizer#parseChildData(EffectiveStatementInference, InputStream)}.
     *
     * @param prefix {@link YangInstanceIdentifier} steps that need to be concatenated to the request path to form
     *               a {@link YangInstanceIdentifier} pointing to the immediate parent of {@link #result}.
     * @param result a {@link NormalizationResult}
     */
    record PrefixAndResult(List<PathArgument> prefix, NormalizationResult<?> result) {
        /**
         * Default constructor.
         *
         * @param prefix {@link YangInstanceIdentifier} steps that need to be concatenated to the request path to form
         *               a {@link YangInstanceIdentifier} pointing to the immediate parent of {@link #result}.
         * @param result parsed data
         */
        public PrefixAndResult {
            prefix = List.copyOf(prefix);
            requireNonNull(result);
        }
    }

    /**
     * Parse the contents of an {@link InputStream} as the contents of a data store.
     *
     * <p>
     * This method's signature is a bit counter-intuitive. {@code rootNamespace} and {@code rootName} collectively
     * encode the expected root element, which may not be expressed in the underlying YANG data model.
     *
     * <p>
     * The reason for this is that YANG does not define an explicit {@link NodeIdentifier} of the datastore root
     * resource, but protocol encodings require this conceptual root to be encapsulated in protocol documents and the
     * approaches taken differ from protocol to protocol. NETCONF operates in terms of YANG-modeled RPC operations,
     * where this conceptual root is given an anchor -- {@code get-config} output's {@code anyxml data}. RESTCONF
     * operates in terms of HTTP payloads and while it models such an anchor, it is rather unnatural
     * {@code container data} with description defining its magic properties and it is not feasible for YANG parser
     * to help us with that.
     *
     * <p>
     * Therefore this method takes the name of the root element in two arguments, which together define its value in
     * both JSON-based (module + localName} and XML-based (namespace + localName) encodings. Implementations of this
     * method are expected to use this information and treat the root element outside of their usual YANG-informed
     * processing.
     *
     * <p>
     * For example, XML parsers will pick {@code containerName.getNodeType().getNamespace()} to match the root element's
     * namespace and {@code containerName.getNodeType().getLocalName()} to match the element's local name. JSON parsers,
     * on the other hand, will use {@code moduleName} and {@code rootName.getLocalName()} to match the top-level JSON
     * object's sole named member.
     *
     * @param containerName expected root container name
     * @param moduleName module name corresponding to {@code containerName}
     * @param stream the {@link InputStream} to parse
     * @return parsed {@link ContainerNode} corresponding to the data store root, with its {@link ContainerNode#name()}
     *         equal to {@code containerName}.
     * @throws NullPointerException if any argument is {@code null}
     * @throws NormalizationException if an error occurs
     */
    NormalizationResult<ContainerNode> parseDatastore(NodeIdentifier containerName, Unqualified moduleName,
        InputStream stream) throws NormalizationException;

    /**
     * Parse the contents of an {@link InputStream} as a data resource.
     *
     * @param inference pointer to the data resource
     * @param stream the {@link InputStream} to parse
     * @return Parsed {@link NormalizedNode} corresponding the requested resource
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code inference} does not to point to a resource recognized by this parser
     * @throws NormalizationException if an error occurs
     */
    NormalizationResult<?> parseData(EffectiveStatementInference inference, InputStream stream)
        throws NormalizationException;

    /**
     * Parse the contents of an {@link InputStream} as a child data resource.
     *
     * @param parentInference pointer to the parent of the data resource
     * @param stream the {@link InputStream} to parse
     * @return A {@link PrefixAndResult} containing parsed resource data and any {@link YangInstanceIdentifier} steps
     *         that need to be appended between {@code inference} and the parsed {@link NormalizedNode}
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code inference} does not to point to a resource recognized by this parser
     * @throws NormalizationException if an error occurs
     */
    PrefixAndResult parseChildData(EffectiveStatementInference parentInference, InputStream stream)
        throws NormalizationException;

    /**
     * Parse the contents of an {@link InputStream} as an operation {@code input}.
     *
     * @param operationInference pointer to the operation
     * @param stream the {@link InputStream} to parse
     * @return Parsed {@link ContainerNode} corresponding to the operation input
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code inference} does not to point to an operation recognized by this parser
     * @throws NormalizationException if an error occurs
     */
    NormalizationResult<ContainerNode> parseInput(EffectiveStatementInference operationInference, InputStream stream)
        throws NormalizationException;

    /**
     * Parse the contents of an {@link InputStream} as on operation {@code output}.
     *
     * @param operationInference pointer to the operation
     * @param stream the {@link InputStream} to parse
     * @return Parsed {@link ContainerNode} corresponding to the operation output
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code inference} does not to point to an operation recognized by this parser
     * @throws NormalizationException if an error occurs
     */
    NormalizationResult<ContainerNode> parseOutput(EffectiveStatementInference operationInference, InputStream stream)
        throws NormalizationException;
}
