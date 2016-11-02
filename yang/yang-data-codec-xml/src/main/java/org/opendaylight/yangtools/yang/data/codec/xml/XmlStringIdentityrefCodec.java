/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.base.Preconditions;
import java.net.URI;
import javax.annotation.Nonnull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.util.ModuleStringIdentityrefCodec;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class XmlStringIdentityrefCodec extends ModuleStringIdentityrefCodec implements XmlCodec<QName> {

    private final NamespaceContext namespaceContext;

    XmlStringIdentityrefCodec(final SchemaContext context, final QNameModule parentModule,
                              final NamespaceContext namespaceContext) {
        super(context, parentModule);
        this.namespaceContext = Preconditions.checkNotNull(namespaceContext);
    }

    @Override
    protected Module moduleForPrefix(@Nonnull final String prefix) {
        if (prefix.isEmpty()) {
            return context.findModuleByNamespaceAndRevision(parentModuleQname.getNamespace(),
                    parentModuleQname.getRevision());
        } else {
            final String prefixedNS = namespaceContext.getNamespaceURI(prefix);
            return context.findModuleByNamespaceAndRevision(URI.create(prefixedNS), null);
        }
    }

    /**
     * Serialize QName with specified XMLStreamWriter.
     *
     * @param writer XMLStreamWriter
     * @param value QName
     */
    @Override
    public void serializeToWriter(final XMLStreamWriter writer, final QName value) throws XMLStreamException {
        writer.writeCharacters(serialize(value));
    }
}
