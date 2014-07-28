package org.opendaylight.yangtools.yang.binding;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Event Stream Writer for Binding Representation
 *
 *
 * <h3>Emmiting Event Stream</h3>
 *
 * <ul>
 * <li><code>container</code> - Container node representation, start event is
 * emitted using {@link #startContainerNode(String)} and node end event is
 * emitted using {@link #endNode()}. Container node is implementing
 * {@link DataObject} interface.
 *
 * <li><code>list</code> - YANG list statement has two representation in event
 * stream - unkeyed list and map. Unkeyed list is YANG list which did not
 * specify key.</li>
 *
 * <ul>
 * <li><code>Map</code> - Map start event is emitted using
 * {@link #startMapNode(String)} and is ended using {@link #endNode()}. Each map
 * entry start is emitted using {@link #startMapEntryNode(Map)} with Map of keys
 * and finished using {@link #endNode()}.</li>
 *
 * <li><code>UnkeyedList</code> - Unkeyed list represent list without keys,
 * unkeyed list start is emmited using {@link #startUnkeyedList(String)} list
 * end is emmited using {@link #endNode()}. Each list item is emmited using
 * {@link #startUnkeyedListItem()} and ended using {@link #endNode()}.</li>
 * </ul>
 *
 * <li><code>leaf</code> - Leaf node event is emitted using
 * {@link #leafNode(String, Object)}. {@link #endNode()} MUST be not emmited for
 * leaf node.</li>
 *
 * <li><code>leaf-list</code> - Leaf list start is emitted using
 * {@link #startLeafSet(String)}. Leaf list end is emitted using
 * {@link #endNode()}. Leaf list entries are emmited using
 * {@link #leafSetEntryNode(Object).
 *
 * <li><code>anyxml - Anyxml node event is emitted using
 *
 *
 * {@link #leafNode(String, Object)}. {@link #endNode()} MUST be not emmited
 * for anyxml node.</code></li>
 *
 *
 * <li><code>choice</code> Choice node event is emmited by
 * {@link #startChoiceNode(String)} event and must be immediately followed by
 * {@link #startCase(QName)} event. Choice node is finished by emitting
 * {@link #endNode()} event.</li>
 *
 * <li>
 * <code>case</code> - Case node may be emitted only inside choice node by
 * invoking {@link #startCase(QName)}. Case node is finished be emitting
 * {@link #endNode()} event.</li>
 *
 * <li>
 * <code>augment</code> - Represents augmentation, augmentation node is started
 * by invoking {@link #startAugmentationNode(QNameModule, String...)} and
 * finished by invoking {@link #endNode()}.</li>
 *
 * </ul>
 *
 * <h3>Implementation notes</h3> This interface is not intended to be
 * implemented by users of generated Binding DTOs but to be used by utilities,
 * which needs to emit NormalizedNode model from Binding DTOs.
 * <p>
 * This interface is intended as API definition of facade for real Event /
 * Stream Writer, without explicitly requiring stream writer and related
 * interfaces to be imported by all generated Binding DTOs.
 * <p>
 * Existence of this interface in base Java Binding package is required to
 * support runtime generation of users of this interface in OSGI and OSGI-like
 * environment, since this package is only package which is imported by all
 * generated Binding DTOs and wired in OSGI.
 *
 *
 */
public interface BindingStreamEventWriter {

    /**
     * Returns current location context of Stream Event Writer.
     * <p>
     * Location context represents conceptual location in data tree, which is
     * currently emited and it is modified by invocation of
     * <code>start*Node</code>, which appends computed QName to context location
     * and by {@link #endNode()} which removes last QName for location.
     *
     * <p>
     * Note: This may be required for correct implementation of emmiting events
     * for {@link #startCase(QName)} and
     * {@link #startAugmentationNode(QNameModule, String...)} where emmiter
     * needs to compute namespace for node, which is emmited.
     *
     * Namespace may be affected not only by augmentation state, but also if
     * that augmentation / node was added via uses of augmented grouping or it
     * was normal augmentation.
     *
     * @return QName Conceptual location context of current node event, which
     *         was not finished by emiting {@link #endNode()}.
     */
    Iterable<QName> getCurrentLocationContext();

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
    void leafNode(String localName, Object value) throws IllegalArgumentException;

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
     * @throws IllegalArgumentException
     *             If emitted leaf node is invalid in current context or was
     *             emitted multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startLeafSet(String localName) throws IllegalArgumentException;

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
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startContainerNode(String localName) throws IllegalArgumentException;

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
     * @throws IllegalArgumentException
     *             If emitted node is invalid in current context or was emitted
     *             multiple times.
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startUnkeyedList(String localName) throws IllegalArgumentException;

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
     *
     *
     *
     * @throws IllegalStateException
     *             If node was emitted outside <code>unkeyed list</code> node.
     */
    void startUnkeyedListItem() throws IllegalStateException;

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
    void startMapNode(String localName) throws IllegalArgumentException;

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
    void startMapEntryNode(Map<QName, Object> key) throws IllegalArgumentException;

    /**
     *
     *
     *
     * @param localName
     *            name of node as defined in schema, namespace and revision are
     *            derived from parent node.
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             If node was emitted inside <code>map</code>,
     *             <code>choice</code> <code>unkeyed list</code> node.
     */
    void startChoiceNode(String localName) throws IllegalArgumentException;

    /**
     *
     *
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
     * @param name
     * @throws IllegalArgumentException
     */
    void startCase(QName name) throws IllegalArgumentException;

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
     * @param module
     *            QName module of YANG module in which augmentation was defined
     * @param possibleChildren
     *            Local names of all valid children defined by augmentation.
     * @throws IllegalArgumentException
     *             If augmentation is invalid in current context.
     */
    void startAugmentationNode(QNameModule module, String... possibleChildren) throws IllegalArgumentException;

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
    void anyxmlNode(String name, Object value) throws IllegalArgumentException;

    /**
     * Emits end event for node.
     *
     */
    void endNode();
}
