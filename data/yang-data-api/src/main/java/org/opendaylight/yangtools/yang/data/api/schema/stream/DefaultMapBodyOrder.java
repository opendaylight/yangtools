/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Default, standards-compliant mode. {@code leaf} children referenced in predicates are first, followed by others
 * in iteration order.
 */
@NonNullByDefault
final class DefaultMapBodyOrder extends MapBodyOrder {
    static final DefaultMapBodyOrder INSTANCE = new DefaultMapBodyOrder();

    private DefaultMapBodyOrder() {
        // Hidden on purposa
    }

    @Override
    Iterable<DataContainerChild> orderBody(final MapEntryNode entry) throws IOException {
        // First things first: we will need to size our two collections...
        final var keys = entry.name().keySet();
        final var keySize = keys.size();
        final var entrySize = entry.size();
        final var otherSize = entrySize - keySize;

        // ... and that allows us to establish some invariants and optimize based on them
        if (otherSize > 0) {
            return orderBody(entry, keys, keySize, otherSize);
        } else if (otherSize == 0) {
            return keySize == 1 ? orderKey(entry, keys.iterator().next()) : orderKeys(entry, keys, keySize);
        } else {
            throw new IOException(entry.name() + " requires " + keySize + ", have only " + entrySize);
        }
    }

    private static Iterable<DataContainerChild> orderBody(final MapEntryNode entry, final Set<QName> qnames,
            final int keySize, final int otherSize) throws IOException {
        final var keys = new ArrayList<LeafNode<?>>(keySize);
        final var others = new ArrayList<DataContainerChild>(otherSize);

        // Single-pass over children, classifying them into two parts.
        for (var child : entry.body()) {
            if (child instanceof LeafNode<?> leaf && qnames.contains(qnameOf(leaf))) {
                keys.add(leaf);
            } else {
                others.add(child);
            }
        }

        // Check we have all the keys
        if (keys.size() != keySize) {
            throw new IOException("Missing leaf nodes for "
                + Sets.difference(qnames, keys.stream().map(DefaultMapBodyOrder::qnameOf).collect(Collectors.toSet()))
                + " in " + entry);
        }

        // Make sure key iteration order matches qnames, if not go through a sort
        if (!Iterators.elementsEqual(qnames.iterator(),
                Iterators.transform(keys.iterator(), DefaultMapBodyOrder::qnameOf))) {
            sortKeys(keys, qnames);
        }

        return Iterables.concat(keys, others);
    }

    private static Iterable<DataContainerChild> orderKeys(final MapEntryNode entry, final Set<QName> qnames,
            final int keySize) throws IOException {
        // Every child is supposed to be a leaf, addressable via NodeIdentifier, just look each one up and be done with
        // it.
        final var keys = new ArrayList<DataContainerChild>(keySize);
        for (var qname : qnames) {
            keys.add(requireKeyLeaf(entry, qname));
        }
        return keys;
    }

    private static Collection<DataContainerChild> orderKey(final MapEntryNode entry, final QName key)
            throws IOException {
        requireKeyLeaf(entry, key);
        return entry.body();
    }

    private static LeafNode<?> requireKeyLeaf(final MapEntryNode entry, final QName key) throws IOException {
        final var child = entry.childByArg(new NodeIdentifier(key));
        if (child instanceof LeafNode<?> leaf) {
            return leaf;
        } else if (child == null) {
            throw new IOException("No leaf for " + key + " in " + entry.prettyTree());
        } else {
            throw new IOException("Child " + child + " is not a leaf");
        }
    }

    private static void sortKeys(final ArrayList<LeafNode<?>> keys, final Set<QName> qnames) {
        throw new UnsupportedOperationException();
    }

    private static QName qnameOf(final NormalizedNode node) {
        return node.name().getNodeType();
    }
}