/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An internal base class for common methods shared between {@link Attribute} and {@link Element}. These are related to
 * while XML applies namespace inheritance differently, both XML attributes and XML elements have the notion of being
 * identified by a {@link #namespace()} and a {@link #localName()}
 */
public sealed interface Node permits Attribute, Element, ImmutableNode, W3CNode {
    /**
     * Return this node local name..
     *
     * @return A local name string
     */
    @NonNull String localName();

    /**
     * Return this node's optional namespace.
     *
     * @return A namespace string, or {@code null}
     */
    @Nullable String namespace();
}
