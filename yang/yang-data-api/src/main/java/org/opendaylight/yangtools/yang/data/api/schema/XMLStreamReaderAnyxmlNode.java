/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.schema.XMLStreamReaderAnyxmlNode.Body;

// FIXME: move this to yang.data.spi
@Beta
public interface XMLStreamReaderAnyxmlNode extends AnyxmlNode<Body> {
    /**
     * A {@link AnyxmlNode} body which can be converted to an {@link XMLStreamReader}.
     */
    @Beta
    @FunctionalInterface
    public interface Body extends Immutable {
        /**
         * Return {@link XMLStreamReader} reporting events constituting this node. Returned the stream contains the
         * {@code anyxml} node as the root element.
         * @return A XMLStreamReader
         */
        @NonNull XMLStreamReader toXMLStreamReader();
    }

    @Override
    default Class<Body> bodyObjectModel() {
        return Body.class;
    }
}
