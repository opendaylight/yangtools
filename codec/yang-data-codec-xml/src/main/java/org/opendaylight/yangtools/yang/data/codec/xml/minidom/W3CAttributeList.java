/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.DoNotCall;
import java.util.AbstractList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

final class W3CAttributeList extends AbstractList<@NonNull Attribute> {
    private final NamedNodeMap nodeMap;

    W3CAttributeList(final NamedNodeMap nodeMap) {
        this.nodeMap = requireNonNull(nodeMap);
    }

    @Override
    public Attribute get(final int index) {
        final var item = (Attr) nodeMap.item(index);
        if (item == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return new W3CAttribute(item);
    }

    @Override
    public int size() {
        return nodeMap.getLength();
    }

    @Override
    @Deprecated(forRemoval = true)
    @DoNotCall("Always throws UnsupportedOperationException")
    public boolean addAll(final Collection<? extends Attribute> collection) {
        throw new UnsupportedOperationException();
    }
}
