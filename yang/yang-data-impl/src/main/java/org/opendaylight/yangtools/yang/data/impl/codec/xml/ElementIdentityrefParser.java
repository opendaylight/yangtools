/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Preconditions;
import java.net.URI;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.util.AbstractStringIdentityrefCodec;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

@Deprecated
final class ElementIdentityrefParser extends AbstractStringIdentityrefCodec {
    private final SchemaContext schema;
    private final Element element;

    ElementIdentityrefParser(final SchemaContext schema, final Element element) {
        this.element = Preconditions.checkNotNull(element);
        this.schema = Preconditions.checkNotNull(schema);
    }

    @Override
    protected String prefixForNamespace(@Nonnull final URI namespace) {
        return element.lookupPrefix(namespace.toString());
    }

    @Override
    protected QName createQName(@Nonnull final String prefix, @Nonnull final String localName) {
        final String namespace = element.lookupNamespaceURI(!prefix.isEmpty() ? prefix : null);
        Preconditions.checkArgument(namespace != null, "Failed to lookup prefix %s", prefix);

        final URI ns = URI.create(namespace);
        final Module module = schema.findModuleByNamespaceAndRevision(ns, null);
        Preconditions.checkArgument(module != null, "Namespace %s is not owned by a module", ns);
        return QName.create(module.getQNameModule(), localName);
    }

}
