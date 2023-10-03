/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
final class ImmutableTextElement extends ImmutableElement implements TextElement {
    private final String text;

    ImmutableTextElement(final @Nullable String namespace, final String localName, final List<Attribute> attributes,
            final String text) {
        super(namespace, localName, attributes);
        this.text = requireNonNull(text);
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("text", text);
    }
}
