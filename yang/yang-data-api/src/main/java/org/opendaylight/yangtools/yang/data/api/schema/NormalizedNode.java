package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.Node;

/**
 *
 * Node which is normalized according to the YANG schema
 * is identifiable by {@link InstanceIdentifier}.
 *
 *
 * @author Tony Tkacik
 *
 * @param <K> Local identifier of node
 * @param <V> Value of node
 */
public interface NormalizedNode<K extends InstanceIdentifier.PathArgument,V> extends
    Identifiable<K>, //
    Node<V> {

    /**
     *
     * QName of the node as defined in YANG schema.
     *
     */
    @Override
    public QName getNodeType();

    /**
     *
     * Locally unique identifier of nodes
     *
     */
    @Override
    public K getIdentifier();

    /**
     *
     * Value of node
     *
     */
    @Override
    public V getValue();

}
