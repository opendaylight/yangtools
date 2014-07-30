/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

public interface NormalizedNodeStreamWriter {

    /**
     *
     * Emits a leaf node event with supplied value.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param value
     *            Value of leaf node.
     * @throws IllegalArgumentException
     *             If emitted leaf node has invalid value in current context or
     *             was emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void leafNode(NodeIdentifier localName, Object value) throws IllegalArgumentException;

    /**
     *
     * Emits a start of leaf set (leaf-list).
     * <p>
     * Emits start of leaf set, during writing leaf set event, only
     * {@link #leafSetEntryNode(Object)} calls are valid. Leaf set event is
     * finished by calling {@link #endNode()}.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     * @throws IllegalArgumentException
     *             If emitted leaf node is invalid in current context or was
     *             emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startLeafSet(NodeIdentifier localName, int childSizeHint) throws IllegalArgumentException;

    /**
     * Emits a leaf set entry node
     *
     * @param value
     *            Value of leaf set entry node.
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
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(String)}</li>
     * <li>{@link #startChoiceNode(String)}</li>
     * <li>{@link #startLeafSet(String)}</li>
     * <li>{@link #startMapNode(String)}</li>
     * <li>{@link #startUnkeyedList(String)}</li>
     * <li>{@link #startAugmentationNode(QNameModule, String...)}</li>
     * </ul>
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startContainerNode(NodeIdentifier localName, int childSizeHint) throws IllegalArgumentException;

    /**
     *
     * Emits start of unkeyed list node event.
     *
     * <p>
     * End of unkeyed list event is emitted by invoking {@link #endNode()}.
     * Valid subevents is only {@link #startUnkeyedListItem()}. All other
     * methods will throw {@link IllegalArgumentException}.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startUnkeyedList(NodeIdentifier localName, int childSizeHint) throws IllegalArgumentException;

    /**
     * Emits start of new unkeyed list item.
     *
     * <p>
     * Unkeyed list item event is finished by invoking {@link #endNode()}. Valid
     * sub-events are:
     * <ul>
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(String)}</li>
     * <li>{@link #startChoiceNode(String)}</li>
     * <li>{@link #startLeafSet(String)}</li>
     * <li>{@link #startMapNode(String)}</li>
     * <li>{@link #startUnkeyedList(String)}</li>
     * <li>{@link #startAugmentationNode(QNameModule, String...)}</li>
     * </ul>
     * @param childSizeHint
     *
     *
     *
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
     * subevents is only {@link #startMapNode(String)}. All other methods will
     * throw {@link IllegalArgumentException}.
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startMapNode(NodeIdentifier localName, int childSizeHint) throws IllegalArgumentException;

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
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(String)}</li>
     * <li>{@link #startChoiceNode(String)}</li>
     * <li>{@link #startLeafSet(String)}</li>
     * <li>{@link #startMapNode(String)}</li>
     * <li>{@link #startUnkeyedList(String)}</li>
     * <li>{@link #startAugmentationNode(QNameModule, String...)}</li>
     * </ul>
     * @param childSizeHint
     *
     * @param key
     *            QName to value pairs of keys of map entry node.
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
    * subevents is only {@link #startMapNode(String)}. All other methods will
    * throw {@link IllegalArgumentException}.
    *
    * @param localName
    *            name of node as defined in schema, namespace and revision are
    *            derived from parent node.
    * @throws IllegalArgumentException
    * @throws IllegalStateException
    *             If node was emitted inside <code>map</code>,
    *             <code>choice</code> <code>unkeyed list</code> node.
    */
   void startOrderedMapNode(NodeIdentifier localName) throws IllegalArgumentException;

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
    * <li>{@link #leafNode(String, Object)}</li>
    * <li>{@link #startContainerNode(String)}</li>
    * <li>{@link #startChoiceNode(String)}</li>
    * <li>{@link #startLeafSet(String)}</li>
    * <li>{@link #startMapNode(String)}</li>
    * <li>{@link #startUnkeyedList(String)}</li>
    * <li>{@link #startAugmentationNode(QNameModule, String...)}</li>
    * </ul>
    *
    * @param key
    *            QName to value pairs of keys of map entry node.
    * @throws IllegalArgumentException
    *             If key contains incorrect value.
    * @throws IllegalStateException
    *             If node was emitted outside <code>map entry</code> node.
    */
   void startOrderedMapEntryNode(NodeIdentifierWithPredicates identifier) throws IllegalArgumentException;

    /**
     *
     *
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @param childSizeHint
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startChoiceNode(NodeIdentifier localName, int childSizeHint) throws IllegalArgumentException;

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
     * <li>{@link #leafNode(String, Object)}</li>
     * <li>{@link #startContainerNode(String)}</li>
     * <li>{@link #startChoiceNode(String)}</li>
     * <li>{@link #startLeafSet(String)}</li>
     * <li>{@link #startMapNode(String)}</li>
     * <li>{@link #startUnkeyedList(String)}</li>
     * </ul>
     *
     * @param identifier Augmentation identifier
     * @throws IllegalArgumentException
     *             If augmentation is invalid in current context.
     */
    void startAugmentationNode(AugmentationIdentifier identifier) throws IllegalArgumentException;

    /**
     * Emits anyxml node event.
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
     */
    void endNode();

}
