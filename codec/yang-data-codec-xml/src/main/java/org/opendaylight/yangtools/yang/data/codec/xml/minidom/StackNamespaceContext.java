/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 */
final class StackNamespaceContext implements NamespaceContext {
    private final Deque<Element> stack = new ArrayDeque<>();

    @Override
    public String getNamespaceURI(final String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPrefix(final String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(final String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
    }

    @Nullable Element current() {
        return stack.peek();
    }



}
