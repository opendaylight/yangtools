/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Definition of YANG Data Object Model. This model is similar in some respects to the
 * {@code W3C Document Object Model} (W3C DOM), but is a lot more baroque owing to:
 * <ul>
 *   <li>the inherent complexities of YANG and the its language extensions</li>
 *   <li>the fact this a normalized view, e.g. it is bound to an {@link EffectiveModelContext} view of the piece of data
 *       being modelled</li>
 * </ul>
 *
 * <p>
 * There are three basic building blocks:
 * <ul>
 *   <li>{@link NormalizedTree}, which is akin to a {@code W3C DOM Document}</li>
 *   <li>{@link NormalizedData}, which is akin to a {@code W3C DOM Element}</li>
 *   <li>NormalizedMetadata, which is akin to a {@code W3C DOM Attr}</li>
 * </ul>
 * There are two critical differences between this model and the Document Object Model:
 * <ol>
 *   <li>there is no all-encompassing {@code Node} interface, which would provide general navigability. This difference
 *       stems from our goal to ensure exact semantics, acknowledging different concepts have inherently different
 *       lifecycle. In particular, data tends to be stored and transferred verbatim, while metadata tends to be
 *       discarded and re-generated, and
 *   </li>
 *   <li>this model does <em>NOT</em> provide the ability to navigate to parent or siblings. This is done on purpose, as
 *       implementations of this interfaces are expected to be {@code effective immutable} (to aid
 *       multi-threaded programming), while at the same time allowing for their efficient implementation by a YANG
 *       datastore using
 *       <a href="https://en.wikipedia.org/wiki/Multiversion_concurrency_control">Multiversion concurrency control</a>.
 *   </li>
 * </ol>
 *
 * FIXME: revise the below
 *
 * <h2>Normalized DOM Model</h2>
 *
 * <h3>Node Types</h3>
 * <ul>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode} -
 * Base type representing a node in a tree structure; all nodes are derived from
 * it, it contains a leaf identifier and a value.
 * <ul>
 * <li>
 * {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode} -
 * Node which contains multiple leafs; it does not have a direct representation
 * in the YANG syntax.
 * <ul>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.ContainerNode} -
 * Node, which represents a leaf which can occur only once per parent node; it
 * contains multiple child leaves and maps to the <i>container</i> statement in
 * YANG.</li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode} -
 * Node which represents a leaf, which can occur multiple times; a leave is
 * uniquely identified by the value of its key. A MapEntryNode may contain
 * multiple child leaves. MapEntryNode maps to the instance of <i>list</i> in
 * YANG.</li>
 * <li>
 * {@link org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode}
 * - Node which represents a leaf, which can occur multiple times; a leave is
 * uniquely identified by the value of its key. A MapEntryNode may contain
 * multiple child leaves. MapEntryNode maps to the instance of <i>list</i> in
 * YANG.</li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode} - Node
 * which represents a leaf, which occurs mostly once per parent node, but
 * possible values could have different types. Maps to <i>choice</i> statement.
 * Types maps to the <i>case</i> statements for that <i>choice</i>.</li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode}
 * - Node which represents a leaf, which occurs mostly once per parent node.</li>
 * </ul>
 * </li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode} - Node
 * which represents a leaf, which occurs mostly once per parent node. Contains
 * simple value.</li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode}
 * - Node which represents a leaf, which type could occurs multiple times per
 * parent node. Maps to to the instances of <i>leaf-list</i> in YANG.</li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode} -
 * Special node, which can occur only once per parent node; its leaves are
 * LeafSetEntryNode nodes of specified type. Maps into the <i>leaf-list</i> in
 * YANG.</li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.MapNode} - Special
 * node, which can occur only once per parent node; its leaves are MapEntryNode
 * nodes.
 * <ul>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.UserMapNode} -
 * Special node, which can occur only once per parent node; its leaves are
 * MapEntryNode nodes.</li>
 * </ul>
 * </li>
 * <li> {@link org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode} -
 * Special node, which can occur only once per parent node; its leaves are
 * MapEntryNode nodes.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>Tree / subtree structure</h3> <h4>Grammar representation</h4>
 *
 * <pre>
 *  {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument}*
 *  {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier}
 *    | {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates}
 *    | {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue}
 *    | {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier}
 *
 *  TreeRoot = {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode}
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode} =
 *    ( {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode}
 *     | {@link org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode}
 *     | {@link org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode}
 *     | {@link org.opendaylight.yangtools.yang.data.api.schema.MapNode}
 *     | {@link org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode})*
 *  ContainerDataNode =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier}
 *    {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode}
 *
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier} SimpleValue
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier}
 *    {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode}
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.MapNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier}
 *    {@link org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode}
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates}
 *    {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode}
 *
 *  // Special nodes
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier}
 *    {@link org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode}*
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier}
 *    {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode}
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue} SimpleValue
 * </pre>
 *
 * <p>
 * The resulting tree organization is following:
 *
 * <ul>
 *   <li>(DataContainerNode)
 * <ul>
 * <li>(0..n) LeafNode</li>
 * <li>(0..n) LeafSetNode
 * <ul>
 * <li>(0..n) LeafSetEntryNode</li>
 * </ul>
 * </li>
 * <li>(0..n) ContainerNode
 * <ul>
 * <li>(Same as DataContainerNode)</li>
 * </ul>
 * </li>
 * <li>(0..n) ContainerNode
 * <ul>
 * <li>(Same as DataContainerNode)</li>
 * </ul>
 * </li>
 * <li>(0..n) MapNode
 * <ul>
 * <li>(0..n) MapEntryNode
 * <ul>
 * <li>(Same as DataContainerNode)</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>(0..n) AugmentationNode
 * <ul>
 * <li>(Same as DataContainerNode)</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>Ordering of child nodes</h3>
 * Ordering of child nodes is not enforced by this API definition, unless explicitly stated by subclasses of
 * {@link org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer}, which marks nodes with semantic
 * constraint to preserve user-supplied ordering.
 *
 * <p>
 * Clients should not expect any specific ordering of child nodes for interfaces from this package which does not extend
 * {@link org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer}, since implementations are not required
 * to have well-defined order, which allows for more efficient implementations. If such ordering is required by clients
 * for serialization / debugability it SHOULD be done externally in code using these interfaces.
 */
package org.opendaylight.yangtools.yang.data.api.schema;
