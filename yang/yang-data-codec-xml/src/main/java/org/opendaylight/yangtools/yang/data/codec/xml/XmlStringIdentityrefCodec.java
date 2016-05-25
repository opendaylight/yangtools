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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringIdentityrefCodec;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class XmlStringIdentityrefCodec extends AbstractModuleStringIdentityrefCodec implements XmlCodec<QName> {

    private final SchemaContext context;
    private final QNameModule parentModuleQname;

    XmlStringIdentityrefCodec(final SchemaContext context, final QNameModule parentModule) {
        this.context = Preconditions.checkNotNull(context);
        this.parentModuleQname = Preconditions.checkNotNull(parentModule);
    }

    @Override
    protected Module moduleForPrefix(final String prefix) {
        if (prefix.isEmpty()) {
            return context.findModuleByNamespaceAndRevision(parentModuleQname.getNamespace(),
                    parentModuleQname.getRevision());
        } else {
            return context.findModuleByName(prefix, null);
        }
    }

    @Override
    protected String prefixForNamespace(final URI namespace) {
        final Module module = context.findModuleByNamespaceAndRevision(namespace, null);
        return module == null ? null : module.getName();
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
