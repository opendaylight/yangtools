/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;

import javax.xml.transform.dom.DOMSource;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.yang.binding.OpaqueData;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ForeignDataNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;

final class AnyxmlCodecContext<T extends OpaqueObject<T>> extends AbstractOpaqueCodecContext<T> {
    AnyxmlCodecContext(final AnyxmlSchemaNode schema, final String getterName, final Class<T> bindingClass,
            final BindingClassLoader loader) {
        super(schema, getterName, bindingClass, loader);
    }

    @Override
    AnyxmlNode<?> serializedData(final OpaqueData<?> opaqueData) {
        final var model = opaqueData.getObjectModel();
        verify(DOMSource.class.isAssignableFrom(model), "Cannot just yet support object model %s", model);
        return ImmutableNodes.newAnyxmlBuilder(DOMSource.class)
            .withNodeIdentifier(getDomPathArgument())
            .withValue((DOMSource) opaqueData.getData())
            .build();
    }

    @Override
    T deserialize(final ForeignDataNode<?> foreignData) {
        // Streaming cannot support anything but DOMSource-based AnyxmlNodes.
        verify(foreignData instanceof DOMSourceAnyxmlNode, "Variable node %s not supported yet", foreignData);
        return super.deserialize(foreignData);
    }
}