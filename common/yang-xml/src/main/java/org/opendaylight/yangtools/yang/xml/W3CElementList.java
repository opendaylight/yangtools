/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xml;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.DoNotCall;
import java.util.AbstractList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.w3c.dom.NodeList;

final class W3CElementList extends AbstractList<@NonNull Element> {
    private final NodeList nodeList;

    W3CElementList(final NodeList nodeList) {
        this.nodeList = requireNonNull(nodeList);
    }

    @Override
    public int size() {
        return nodeList.getLength();
    }

    @Override
    public Element get(final int index) {
        final var item = (org.w3c.dom.Element) nodeList.item(index);
        if (item == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return W3CElement.of(item);
    }

    @Override
    @Deprecated(forRemoval = true)
    @DoNotCall("Always throws UnsupportedOperationException")
    public boolean addAll(final Collection<? extends Element> collection) {
        throw new UnsupportedOperationException();
    }
}
