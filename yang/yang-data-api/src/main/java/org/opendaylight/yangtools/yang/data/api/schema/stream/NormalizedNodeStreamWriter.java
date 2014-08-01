/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;


/**
 * Event Stream Writer based on Normalized Node tree representation
 *
 * <h3>Writing Event Stream</h3>
 *
 * <ul>
 * <li><code>container</code> - Container node representation, start event is
 * emitted using {@link #startContainerNode(NodeIdentifier, int)} and node end event is
 * emitted using {@link #endNode()}. Container node is implementing
 * {@link DataObject} interface.
 *
 * <li><code>list</code> - YANG list statement has two representation in event
 * stream - unkeyed list and map. Unkeyed list is YANG list which did not
 * specify key.</li>
 *
 * <ul>
 * <li><code>Map</code> - Map start event is emitted using
 * {@link #startMapNode(NodeIdentifier, int)} and is ended using {@link #endNode()}. Each map
 * entry start is emitted using {@link #startMapEntryNode(NodeIdentifierWithPredicates, int)} with Map of keys
 * and finished using {@link #endNode()}.</li>
 *
 * <li><code>UnkeyedList</code> - Unkeyed list represent list without keys,
 * unkeyed list start is emmited using {@link #startUnkeyedList(NodeIdentifier, int)} list
 * end is emmited using {@link #endNode()}. Each list item is emmited using
 * {@link #startUnkeyedListItem(NodeIdentifier, int)} and ended using {@link #endNode()}.</li>
 * </ul>
 *
 * <li><code>leaf</code> - Leaf node event is emitted using
 * {@link #leafNode(NodeIdentifier, Object)}. {@link #endNode()} MUST NOT BE emmited for
 * leaf node.</li>
 *
 * <li><code>leaf-list</code> - Leaf list start is emitted using
 * {@link #startLeafSet(NodeIdentifier, int)}. Leaf list end is emitted using
 * {@link #endNode()}. Leaf list entries are emmited using
 * {@link #leafSetEntryNode(Object).
 *
 * <li><code>anyxml - Anyxml node event is emitted using
 * {@link #leafNode(NodeIdentifier, Object)}. {@link #endNode()} MUST NOT BE emmited
 * for anyxml node.</code></li>
 *
 *
 * <li><code>choice</code> Choice node event is emmited by
 * {@link #startChoiceNode(NodeIdentifier, int)} event and
 * finished by invoking {@link #endNode()}
 * <li>
 * <code>augment</code> - Represents augmentation, augmentation node is started
 * by invoking {@link #startAugmentationNode(AugmentationIdentifier)} and
 * finished by invoking {@link #endNode()}.</li>
 *
 * </ul>
 *
 * <h3>Implementation notes</h3>
 *
 * <p>
 * Implementations of this interface must not hold user suppled objects
 * and resources needlessly.
 *
 */
public interface NormalizedNodeStreamWriter {

    /**
     * Methods in this interface allow users to hint the underlying
     * implementation about the sizing of container-like constructurs
     * (leafLists, containers, etc.). These hints may be taken into account by a
     * particular implementation to improve performance, but clients are not
     * required to provide hints. This constant should be used by clients who
     * either do not have the sizing information, or do not wish to divulge it
     * (for whatever reasons). Implementations are free to ignore these hints
     * completely, but if they do use them, they are expected to be resilient in
     * face of missing and mismatched hints, which is to say the user can
     * specify startLeafSet(..., 1) and then call leafNode() 15 times.
     * <p>
     * The acceptable hint values are non-negative integers and this constant,
     * all other values will result, based on implementation preference, in the
     * hint being completely ignored or IllegalArgumentException being thrown.
     */
    public final int UNKNOWN_SIZE = -1;

    /**
     *
     * Emits a leaf node event with supplied value.
     *
     * @param name
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param value
     *            Value of leaf node. v
     * @throws IllegalArgumentException
     *             If emitted leaf node has invalid value in current context or
     *             was emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void leafNode(NodeIdentifier name, Object value) throws IllegalArgumentException;

    /**
     *
     * Emits a start of leaf set (leaf-list).
     * <p>
     * Emits start of leaf set, during writing leaf set event, only
     * {@link #leafSetEntryNode(Object)} calls are valid. Leaf set event is
     * finished by calling {@link #endNode()}.
     *
     * @param name
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If emitted leaf node is invalid in current context or was
     *             emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startLeafSet(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException;

    /**
     * Emits a leaf set entry node
     *
     * @param value
     *            Value of leaf set entry node. Supplied object MUST BE constant over time.
     * @throws IllegalArgumentException
     *             If emitted leaf node has invalid value.
     * @throws IllegalStateException
     *             If node was emitted outside <code>leaf set</code> node.
     */
    void leafSetEntryNode(Object value) throws IllegalArgumentException;

    /**
     *
     * Emits start of new container.
     *
     * <p>
     * End of container event is emitted by invoking {@link #endNode()}.
     *
     * <p>
     * Valid sub-events are:
     * <ul>
     * <li>{@link #leafNode}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
    * <li>{@link #startAugmentationNode(AugmentationIdentifier)}</li>
    * </ul>
     *
     * @param name
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startContainerNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException;

    /**
     *
     * Emits start of unkeyed list node event.
     *
     * <p>
     * End of unkeyed list event is emitted by invoking {@link #endNode()}.
     * Valid subevents is only {@link #startUnkeyedListItem(NodeIdentifier, int)}. All other
     * methods will throw {@link IllegalArgumentException}.
     *
     * @param name
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startUnkeyedList(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException;

    /**
     * Emits start of new unkeyed list item.
     *
     * <p>
     * Unkeyed list item event is finished by invoking {@link #endNode()}. Valid
     * sub-events are:
     * <ul>
     * <li>{@link #leafNode}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
     * <li>{@link #startAugmentationNode(AugmentationIdentifier)}</li>
     * </ul>
     *
     * @param name Identifier of node
     * @param childSizeHint
     *            Non-negative count of expected direct child nodes or
     *            {@link #UNKNOWN_SIZE} if count is unknown. This is only hint
     *            and should not fail writing of child events, if there are more
     *            events than count.
     * @throws IllegalStateException
     *             If node was emitted outside <code>unkeyed list</code> node.
     */
    void startUnkeyedListItem(NodeIdentifier name, int childSizeHint) throws IllegalStateException;

    /**
     *
     * Emits start of map node event.
     *
     * <p>
     * End of map node event is emitted by invoking {@link #endNode()}. Valid
     * subevents is only
     * {@link #startMapEntryNode(NodeIdentifierWithPredicates, int)}. All other
     * methods will throw {@link IllegalArgumentException}.
     *
     * @param name
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startMapNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException;

    /**
     *
     * Emits start of map entry.
     *
     * <p>
     * End of map entry event is emitted by invoking {@link #endNode()}.
     *
     * <p>
     * Valid sub-events are:
     * <ul>
     * <li>{@link #leafNode}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
     * <li>{@link #startAugmentationNode(AugmentationIdentifier)}</li>
     * </ul>
     *
     *
     * @param identifier
     *            QName to value pairs of keys of map entry node. Values  MUST BE constant over time.
     * @throws IllegalArgumentException
     *             If key contains incorrect value.
     * @throws IllegalStateException
     *             If node was emitted outside <code>map entry</code> node.
     */
    void startMapEntryNode(NodeIdentifierWithPredicates identifier, int childSizeHint) throws IllegalArgumentException;

    /**
     *
     * Emits start of map node event.
     *
     * <p>
     * End of map node event is emitted by invoking {@link #endNode()}. Valid
     * subevents is only
     * {@link #startMapEntryNode(NodeIdentifierWithPredicates, int)}. All other
     * methods will throw {@link IllegalArgumentException}.
     *
     * @param name
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startOrderedMapNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException;

    /**
     *
     *
     *
     * @param name
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startChoiceNode(NodeIdentifier name, int childSizeHint) throws IllegalArgumentException;

    /**
     * Emits start of augmentation node.
     *
     * <p>
     * End of augmentation event is emitted by invoking {@link #endNode()}.
     *
     * <p>
     * Valid sub-events are:
     *
     * <ul>
     * <li>{@link #leafNode}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
     * </ul>
     *
     * @param identifier
     *            Augmentation identifier
     * @throws IllegalArgumentException
     *             If augmentation is invalid in current context.
     */
    void startAugmentationNode(AugmentationIdentifier identifier) throws IllegalArgumentException;

    /**
     * Emits anyxml node event.
     *
     *
     * @param name
     * @param value
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void anyxmlNode(NodeIdentifier name, Object value) throws IllegalArgumentException;

    /**
     * Emits end event for node.
     *
     * @throws IllegalStateException If there is no start* event to be closed.B
     *
     */
    void endNode() throws IllegalStateException;

}
