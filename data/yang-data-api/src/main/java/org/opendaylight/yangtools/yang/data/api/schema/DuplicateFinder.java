/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

// FIXME: relocate to yang-data-util, where this can have a proper test suite
final class DuplicateFinder {
    private final Map<NormalizedNode, DuplicateEntry> identities = new IdentityHashMap<>();
    private final Map<NormalizedNode, DuplicateEntry> duplicates = new HashMap<>();

    private DuplicateFinder() {
        // Hidden on purpose
    }

    private void findDuplicates(final YangInstanceIdentifier path, final NormalizedNode node) {
        final var i = identities.get(node);
        if (i == null) {
            final var d = duplicates.get(node);
            if (d == null) {
                final var n = new DuplicateEntry(path);
                identities.put(node, n);
                duplicates.put(node, n);
            } else {
                d.addDuplicate(path);
            }

            if (node instanceof NormalizedNodeContainer<?> container) {
                for (var child : container.body()) {
                    findDuplicates(path.node(child.name()), child);
                }
            }
        } else {
            i.addHardLink(path);
        }
    }

    /**
     * Recursively scan a {@link NormalizedNode} instance and its children and produce a collection of
     * {@link DuplicateEntry} objects. Each holds the original definition path and a list of hard/softlinks.
     *
     * @param node Root node, may not be null.
     * @return List of entries
     */
    static Map<NormalizedNode, DuplicateEntry> findDuplicates(final NormalizedNode node) {
        final DuplicateFinder finder = new DuplicateFinder();
        finder.findDuplicates(YangInstanceIdentifier.of(), node);
        return finder.identities;
    }
}
