/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support utilities for dealing with Maps which would normally hold {@link DataContainerChild} values, but are modified
 * to eliminate {@link LeafNode} instances.
 *
 * <p>
 * This class holds implementation logic which controls lifecycle of {@link LeafNode}s  by providing a central policy
 * point for how the implementation treats these nodes. There are two modes of operation:
 * <ul>
 *   <li>eager, which means leaf nodes are retained by their parent<li>
 *   <li>lazy, which means leaf nodes are created whenever they are queried and no attempt is made to retain them</li>
 * </ul>
 *
 * <p>
 * Selection of the mode in effect is available through {@value #EXPENDABLE_PROP_NAME} system property.
 */
@Beta
public final class LazyLeafOperations {
    private static final Logger LOG = LoggerFactory.getLogger(LazyLeafOperations.class);
    private static final String EXPENDABLE_PROP_NAME =
            "org.opendaylight.yangtools.yang.data.impl.schema.nodes.lazy-leaves";

    /**
     * Global enabled run-time constant. If set to true, this class will treat {@link LeafNode} and
     * {@link LeafSetEntryNode} as an expendable object. This constant is controlled by {@value #EXPENDABLE_PROP_NAME}
     * system property.
     */
    private static final boolean EXPENDABLE;

    static {
        EXPENDABLE = Boolean.parseBoolean(System.getProperty(EXPENDABLE_PROP_NAME, "false"));
        LOG.info("Leaf nodes are treated as {} nodes", EXPENDABLE ? "transient" : "regular");
    }

    private LazyLeafOperations() {

    }

    /**
     * A boolean flag indicating whether leaf nodes are being treated as expendable.
     *
     * @return True if NormalizedNode implementations in this artifact are treating leaf nodes as transient, i.e. do
     *              not retain them.
     */
    public static boolean isEnabled() {
        return EXPENDABLE;
    }

    public static Optional<DataContainerChild<?, ?>> findChild(final Map<PathArgument, Object> map,
            final PathArgument key) {
        final Object value = map.get(key);
        return value == null ? Optional.empty() : Optional.of(decodeChild(key, value));
    }

    public static @Nullable DataContainerChild<?, ?> getChild(final Map<PathArgument, Object> map,
            final PathArgument key) {
        final Object value = map.get(key);
        return value == null ? null : decodeChild(key, value);
    }

    public static void putChild(final Map<PathArgument, Object> map, final DataContainerChild<?, ?> child) {
        final DataContainerChild<?, ?> node = requireNonNull(child);
        map.put(node.getIdentifier(), EXPENDABLE ? encodeExpendableChild(node) : node);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static @NonNull Collection<DataContainerChild<?, ?>> getValue(final Map<PathArgument, Object> map) {
        return EXPENDABLE ? new LazyValues(map)
                // This is an ugly cast, but it is accurate IFF all modifications are done through this class
                : (Collection)map.values();
    }

    static @NonNull LeafNode<?> coerceLeaf(final PathArgument key, final Object value) {
        verify(key instanceof NodeIdentifier, "Unexpected value %s for child %s", value, key);
        return ImmutableNodes.leafNode((NodeIdentifier) key, value);
    }

    private static @Nullable DataContainerChild<?, ?> decodeChild(final PathArgument key, final @NonNull Object value) {
        return EXPENDABLE ? decodeExpendableChild(key, value) : verifyCast(value);
    }

    private static @NonNull DataContainerChild<?, ?> decodeExpendableChild(final PathArgument key,
            @NonNull final Object value) {
        return value instanceof DataContainerChild ? (DataContainerChild<?, ?>) value  : coerceLeaf(key, value);
    }

    private static @NonNull Object encodeExpendableChild(final @NonNull DataContainerChild<?, ?> node) {
        return node instanceof LeafNode ? verifyEncode(((LeafNode<?>) node).getValue()) : node;
    }

    private static @NonNull Object verifyEncode(final @NonNull Object value) {
        verify(!(value instanceof DataContainerChild), "Unexpected leaf value %s", value);
        return value;
    }

    private static @NonNull DataContainerChild<?, ?> verifyCast(final @NonNull Object value) {
        verify(value instanceof DataContainerChild, "Unexpected child %s", value);
        return (DataContainerChild<?, ?>)value;
    }
}
