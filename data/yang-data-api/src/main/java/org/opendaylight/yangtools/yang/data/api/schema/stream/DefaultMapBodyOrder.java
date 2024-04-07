/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

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
        final var keysSize = keys.size();
        final var entrySize = entry.size();
        final var otherSize = entrySize - keysSize;

        // ... and that allows us to establish some invariants and optimize based on them
        if (otherSize > 0) {
            return orderBody(entry, keys, keysSize, otherSize);
        } else if (otherSize == 0) {
            return orderKeys(entry, keys, keysSize);
        } else {
            throw new IOException(entry.name() + " requires " + keysSize + ", have only " + entrySize);
        }
    }

    private static Iterable<DataContainerChild> orderBody(final MapEntryNode entry, final Set<QName> qnames,
            final int keySize, final int otherSize) throws IOException {
        final var keys = new ArrayList<DataContainerChild>(keySize);
        final var others = new ArrayList<DataContainerChild>(otherSize);

        //            // Write out all the key children
        //            for (var qname : qnames) {
        //                final DataContainerChild child = node.childByArg(new NodeIdentifier(qname));
        //                if (child != null) {
        //                    write(child);
        //                } else {
        //                    LOG.info("No child for key element {} found", qname);
        //                }
        //            }
        //
        //            // Write all the rest
        //            writeChildren(Iterables.filter(node.body(), input -> {
        //                if (qnames.contains(input.name().getNodeType())) {
        //                    LOG.debug("Skipping key child {}", input);
        //                    return false;
        //                }
        //                return true;
        //            }));
        //
        //
        //            // TODO Auto-generated method stub
        //            return null;
        //        }


        return Iterables.concat(keys, others);
    }

    private static Iterable<DataContainerChild> orderKeys(final MapEntryNode entry, final Set<QName> qnames,
            final int keySize) throws IOException {
        if (keySize == 1) {
            return orderKey(entry, qnames.iterator().next());
        }

        // Every child is supposed to be a leaf, addressable via NodeIdentifier, just look each one up and be done with
        // it
        final var keys = new ArrayList<DataContainerChild>(keySize);



        return keys;
    }

    private static Collection<DataContainerChild> orderKey(final MapEntryNode entry, final QName key)
            throws IOException {
        if (entry.childByArg(new NodeIdentifier(key)) != null) {
            return entry.body();
        }
        throw new IOException("No leaf for " + key + " int " + entry.prettyTree());
    }
}