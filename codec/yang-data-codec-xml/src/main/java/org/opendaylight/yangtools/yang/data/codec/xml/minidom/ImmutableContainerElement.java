/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
final class ImmutableContainerElement extends ImmutableElement implements ContainerElement {
    private final Object children;

    ImmutableContainerElement(final @Nullable String namespace, final String localName,
            final List<Attribute> attributes, final List<Element> children) {
        super(namespace, localName, attributes);
        this.children = maskList(ImmutableList.copyOf(children));
    }

    @Override
    public List<Element> children() {
        return unmaskList(children, Element.class);
    }
}
