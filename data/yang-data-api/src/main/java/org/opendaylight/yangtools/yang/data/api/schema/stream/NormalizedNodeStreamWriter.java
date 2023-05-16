/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.ExtensibleObject;
import org.opendaylight.yangtools.concepts.ObjectExtension;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Event Stream Writer based on Normalized Node tree representation.
 *
 * <h2>Writing Event Stream</h2>
 * Each entity is emitted by invoking its corresponding {@code start*} event, optionally followed by interior events and
 * invoking {@link #endNode()}. Some entities supported nested entities, some do not, see below for restrictions.
 *
 * <p>
 * While this interface defines basic events, the event stream may be extended through {@link Extension}s, discoverable
 * through {@link #getExtensions()} method. The set of these extensions is immutable during the lifetime of a writer and
 * may be freely cached.
 *
 * <ul>
 * <li>{@code container} - Container node representation, start event is emitted using
 * {@link #startContainerNode(NodeIdentifier, int)}.
 * </li>
 *
 * <li>{@code list} - YANG list statement has two representation in event stream - unkeyed list and map. An unkeyed
 * list is YANG list which did not specify a {@code key} statement. A map is a {@code list} with a {@code key}
 * statement.
 * <ul>
 * <li>{@code Map} - Map start event is emitted using {@link #startMapNode(NodeIdentifier, int)}. Each map entry start
 * is emitted using {@link #startMapEntryNode(NodeIdentifierWithPredicates, int)}.
 * </li>
 * <li>{@code UnkeyedList} - Unkeyed list represent list without keys, unkeyed list start is emitted using
 * {@link #startUnkeyedList(NodeIdentifier, int)}. Each list item is emitted using
 * {@link #startUnkeyedListItem(NodeIdentifier, int)}.</li>
 * </ul>
 * </li>
 *
 * <li>{@code leaf} - Leaf node start event is emitted using {@link #startLeafNode(NodeIdentifier)}. Leaf node values
 * need to be emitted through {@link #scalarValue(Object)}.
 * </li>
 *
 * <li>{@code leaf-list} - Leaf list start is emitted using {@link #startLeafSet(NodeIdentifier, int)}. Individual
 * leaf-list entries are emitted using {@link #startLeafSetEntryNode(NodeWithValue)}.
 *
 * <li>{@code anyxml} - An anyxml node event is emitted using {@link #startAnyxmlNode(NodeIdentifier, Class)}.</li>
 *
 * <li>{@code choice} - Choice node event is emitted by {@link #startChoiceNode(NodeIdentifier, int)} event.</li>
 *
 * <li>{@code augment} - Represents augmentation, augmentation node is started by invoking
 * {@link #startAugmentationNode(AugmentationIdentifier)}.
 * </li>
 * </ul>
 *
 * <h3>Implementation notes</h3>
 *
 * <p>
 * Implementations of this interface must not hold user suppled objects and resources needlessly.
 */
public interface NormalizedNodeStreamWriter extends Closeable, Flushable,
        ExtensibleObject<NormalizedNodeStreamWriter, NormalizedNodeStreamWriter.Extension> {
    /**
     * Methods in this interface allow users to hint the underlying implementation about the sizing of container-like
     * constructors (leafLists, containers, etc.). These hints may be taken into account by a particular implementation
     * to improve performance, but clients are not required to provide hints. This constant should be used by clients
     * who either do not have the sizing information, or do not wish to divulge it (for whatever reasons).
     *
     * <p>
     * Implementations are free to ignore these hints completely, but if they do use them, they are expected to be
     * resilient in face of missing and mismatched hints, which is to say the user can specify startLeafSet(..., 1) and
     * then call leafNode() 15 times.
     *
     * <p>
     * The acceptable hint values are non-negative integers and this constant, all other values will result, based on
     * implementation preference, in the hint being completely ignored or IllegalArgumentException being thrown.
     */
    int UNKNOWN_SIZE = -1;

    /**
     * Emits a start of leaf node event.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If emitted leaf node was emitted multiple times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startLeafNode(NodeIdentifier name) throws IOException;

    /**
     * Emits a start of system-ordered leaf set (leaf-list). While this entity is open,
     * only {@link #startLeafSetEntryNode(NodeWithValue)} calls are valid. Implementations are free to reorder entries
     * within the leaf-list.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If emitted leaf node is invalid in current context or was emitted multiple
     *                                  times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startLeafSet(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits a start of a user-ordered leaf set (leaf-list). While this entity is open, only
     * {@link #startLeafSetEntryNode(NodeWithValue)} calls are valid. Implementations must retain the same entry order.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If emitted leaf node is invalid in current context or was emitted multiple
     *                                  times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startOrderedLeafSet(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits a leaf set entry node.
     *
     * @param name name of the node as defined in the schema.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException if {@code name} does not match enclosing leaf set entity
     * @throws IllegalStateException If node was emitted outside {@code leaf set} node.
     * @throws IOException if an underlying IO error occurs
     */
    void startLeafSetEntryNode(NodeWithValue<?> name) throws IOException;

    /**
     * Emits start of new container. Valid sub-events are:
     * <ul>
     * <li>{@link #startLeafNode}</li>
     * <li>{@link #startAnyxmlNode(NodeIdentifier, Class)}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
     * <li>{@link #startAugmentationNode(AugmentationIdentifier)}</li>
     * </ul>
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException  If emitted node is invalid in current context or was emitted multiple times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startContainerNode(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits start of unkeyed list node event. Valid subevents is only
     * {@link #startUnkeyedListItem(NodeIdentifier, int)}.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If emitted node is invalid in current context or was emitted multiple times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startUnkeyedList(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits start of new unkeyed list item. Valid sub-events are:
     * <ul>
     * <li>{@link #startLeafNode}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
     * <li>{@link #startAugmentationNode(AugmentationIdentifier)}</li>
     * </ul>
     *
     * @param name Identifier of node
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalStateException If node was emitted outside <code>unkeyed list</code> node.
     * @throws IOException if an underlying IO error occurs
     */
    void startUnkeyedListItem(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits start of map node event. Valid subevent is only
     * {@link #startMapEntryNode(NodeIdentifierWithPredicates, int)}.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If emitted node is invalid in current context or was emitted multiple times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startMapNode(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits start of map entry. Valid sub-events are:
     * <ul>
     * <li>{@link #startLeafNode}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
     * <li>{@link #startAugmentationNode(AugmentationIdentifier)}</li>
     * </ul>
     *
     * @param identifier QName to value pairs of keys of map entry node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If key contains incorrect value.
     * @throws IllegalStateException If node was emitted outside {@code map entry} node.
     * @throws IOException if an underlying IO error occurs
     */
    void startMapEntryNode(NodeIdentifierWithPredicates identifier, int childSizeHint) throws IOException;

    /**
     * Emits start of map node event.  Valid subevent is only
     * {@link #startMapEntryNode(NodeIdentifierWithPredicates, int)}.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If emitted node is invalid in current context or was emitted multiple times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startOrderedMapNode(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits start of a choice node event.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param childSizeHint Non-negative count of expected direct child nodes or {@link #UNKNOWN_SIZE} if count is
     *                      unknown. This is only hint and should not fail writing of child events, if there are more
     *                      events than count.
     * @throws NullPointerException if {@code name} is null
     * @throws IllegalArgumentException If emitted node is invalid in current context or was emitted multiple times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    void startChoiceNode(NodeIdentifier name, int childSizeHint) throws IOException;

    /**
     * Emits start of augmentation node. Valid sub-events are:
     * <ul>
     * <li>{@link #startLeafNode}</li>
     * <li>{@link #startContainerNode(NodeIdentifier, int)}</li>
     * <li>{@link #startChoiceNode(NodeIdentifier, int)}</li>
     * <li>{@link #startLeafSet(NodeIdentifier, int)}</li>
     * <li>{@link #startMapNode(NodeIdentifier, int)}</li>
     * <li>{@link #startUnkeyedList(NodeIdentifier, int)}</li>
     * </ul>
     *
     * @param identifier Augmentation identifier
     * @throws NullPointerException if {@code identifier} is null
     * @throws IllegalArgumentException If augmentation is invalid in current context.
     * @throws IOException if an underlying IO error occurs
     */
    void startAugmentationNode(AugmentationIdentifier identifier) throws IOException;

    /**
     * Start emitting a new anydata node identified by name.
     *
     * @param name The name of the anydata element
     * @param objectModel The object model of anydata content
     * @return True if the specified object model is supported by this extension and the process of emitting the node
     *         has started. False if the object model is not supported and the node has not started to be emitted.
     * @throws NullPointerException if any argument is null
     * @throws IOException if an underlying IO error occurs
     */
    @Beta
    boolean startAnydataNode(NodeIdentifier name, Class<?> objectModel) throws IOException;

    /**
     * Emits a start of anyxml node event.
     *
     * @param name name of node as defined in schema, namespace and revision are derived from parent node.
     * @param objectModel The object model of anyxml content
     * @return True if the specified object model is supported by this extension and the process of emitting the node
     *         has started. False if the object model is not supported and the node has not started to be emitted.
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException If emitted node is invalid in current context or was emitted multiple times.
     * @throws IllegalStateException If node was emitted inside {@code map}, {@code choice} or a {@code unkeyed list}
     *                               node.
     * @throws IOException if an underlying IO error occurs
     */
    boolean startAnyxmlNode(NodeIdentifier name, Class<?> objectModel) throws IOException;

    /**
     * Set the value of current anyxml node. This call is only valid within the context in which an anyxml node is open.
     *
     * @param value node value
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if the argument does not represents a valid value
     * @throws IllegalStateException if an anyxml node is not open or if it's value has already been set and this
     *                               implementation does not allow resetting the value.
     * @throws IOException if an underlying IO error occurs
     */
    // FIXME: 7.0.0: this probably should integrated with scalarValue()
    void domSourceValue(DOMSource value) throws IOException;

    /**
     * Emits end event for node.
     *
     * @throws IllegalStateException If there is no start* event to be closed.
     * @throws IOException if an underlying IO error occurs
     */
    void endNode() throws IOException;

    /**
     * Attach the specified {@link DataSchemaNode} to the next node which will get started or emitted. The default
     * implementation does nothing.
     *
     * @param schema DataSchemaNode
     * @throws NullPointerException if the argument is null
     */
    default void nextDataSchemaNode(final @NonNull DataSchemaNode schema) {
        requireNonNull(schema);
    }

    /**
     * Set the value of current node. This call is only valid within the context in which a value-bearing node is open,
     * such as a LeafNode, LeafSetEntryNode.
     *
     * @param value node value, must be effectively immutable
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if the argument does not represents a valid value
     * @throws IllegalStateException if a value-bearing node is not open or if it's value has already been set and this
     *                               implementation does not allow resetting the value.
     * @throws IOException if an underlying IO error occurs
     */
    void scalarValue(@NonNull Object value) throws IOException;

    @Override
    void close() throws IOException;

    @Override
    void flush() throws IOException;

    /**
     * Extension interface for {@link NormalizedNodeStreamWriter}. Extensions should extend this interface and their
     * instances should be made available through {@link NormalizedNodeStreamWriter#getExtensions()}.
     */
    interface Extension extends ObjectExtension<NormalizedNodeStreamWriter, Extension> {
        // Marker interface
    }

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
    interface MetadataExtension extends Extension {
        /**
         * Emit a block of metadata associated with the currently-open node. The argument is a map of annotation names,
         * as defined {@code md:annotation} extension. Values are normalized objects, which are required to be
         * effectively-immutable.
         *
         * @param metadata Metadata block
         * @throws NullPointerException if {@code metadata} is {@code null}
         * @throws IllegalStateException when this method is invoked outside of an open node or metadata has already
         *                               been emitted.
         * @throws IOException if an underlying IO error occurs
         */
        void metadata(ImmutableMap<QName, Object> metadata) throws IOException;

        /**
         * Indicate whether metadata is required to be emitted just after an entry is open. The default implementation
         * returns false.
         *
         * @return {@code true} if metadata must occur just after the start of an entry.
         */
        default boolean requireMetadataFirst() {
            return false;
        }
    }

    /**
     * An {@link Extension} exposed by stream writers which can handle mount point data, notably providing
     * the facilities to resolve a mount point schema and normalize mount point contents into a normalized structure.
     */
    @NonNullByDefault
    interface MountPointExtension extends Extension {
        /**
         * Start a new mount point with a specific mount point context. The returned writer will be used to emit the
         * content of the mount point, without touching the writer to which this extension is attached to. Once that is
         * done, the returned writer will be {@link NormalizedNodeStreamWriter#close()}d, at which point the parent
         * writer will be used again to emit the rest of the tree.
         *
         * @param label Mount point label
         * @param mountCtx Mount point context
         * @return A new NormalizedNodeStreamWriter
         * @throws IOException if an error occurs
         */
        NormalizedNodeStreamWriter startMountPoint(MountPointLabel label, MountPointContext mountCtx)
            throws IOException;
    }
}
