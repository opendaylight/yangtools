/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;

final class ImmutableDOMSourceAnyxmlNode extends AbstractAnyxmlNode<DOMSource> implements DOMSourceAnyxmlNode {
    private final @NonNull NodeIdentifier name;
    private final @NonNull DOMSource value;

    ImmutableDOMSourceAnyxmlNode(final NodeIdentifier name, final DOMSource value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }

    @Override
    public NodeIdentifier name() {
        return name;
    }

    @Override
    protected DOMSource value() {
        return value;
    }

    @Override
    protected DOMSource wrappedValue() {
        return value;
    }
}