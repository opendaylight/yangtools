/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Entry describing a duplicate found in a {@link NormalizedNode} tree. Note this
 * class leaks mutable collections, so users are advised either not to share it.
 */
public final class DuplicateEntry implements Identifiable<YangInstanceIdentifier> {
    private final List<YangInstanceIdentifier> hardLinks = new ArrayList<>(1);
    private List<YangInstanceIdentifier> duplicates = Collections.emptyList();

    DuplicateEntry(final YangInstanceIdentifier path) {
        hardLinks.add(path);
    }

    void addDuplicate(final YangInstanceIdentifier path) {
        if (duplicates.isEmpty()) {
            duplicates = new ArrayList<>();
        }
        duplicates.add(path);
    }

    void addHardLink(final YangInstanceIdentifier path) {
        hardLinks.add(path);
    }

    @Override
    public YangInstanceIdentifier getIdentifier() {
        return hardLinks.get(0);
    }

    public List<YangInstanceIdentifier> getHardLinks() {
        return hardLinks;
    }

    public List<YangInstanceIdentifier> getDuplicates() {
        return duplicates;
    }
}