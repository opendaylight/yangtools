/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Definition of normalized YANG DOM Model. Normalized DOM Model brings more direct mapping between YANG Model, DOM
 * representation of data.
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
 *
 *  TreeRoot = {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode}
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode} =
 *    ( {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode}
 *     | {@link org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode}
 *     | {@link org.opendaylight.yangtools.yang.data.api.schema.MapNode}
 *     | {@link org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode})*
 *  ContainerDataNode =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier}
 *    {@link org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode}
 *
 *  {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode} =
 *    {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier} SimpleValue
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
 * <li>(DataContainerNode)
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
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>Ordering of child nodes</h3>
 * Ordering of child nodes is not enforced by this API definition, unless
 * explicitly stated by subclasses of
 * {@link org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer}
 * which marks nodes with semantic constrain to preserve user-supplied ordering.
 *
 * <p>
 * Clients should not expect any specific ordering of child nodes for interfaces
 * from this package which does not extend
 * {@link org.opendaylight.yangtools.yang.data.api.schema.OrderedNodeContainer},
 * since implementations are not required to have well-defined order, which
 * allows for more efficient implementations. If such ordering is required by
 * clients for serialization / debugability it SHOULD be done externally in
 * code using these interfaces.
 */
package org.opendaylight.yangtools.yang.data.api.schema;
