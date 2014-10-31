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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.util.AbstractStringIdentityrefCodec;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class ElementIdentityrefContextParser extends AbstractStringIdentityrefCodec {

    private final String elementNamespace;
    private final String xPathArgument;
    private final SchemaContext schemaContext;

    public ElementIdentityrefContextParser(final String namespace, final String xPathArgument, final SchemaContext schemaContext) {
        this.elementNamespace = namespace;
        this.xPathArgument = xPathArgument;
        this.schemaContext = schemaContext;
    }

    @Override
    protected String prefixForNamespace(URI namespace) {
        throw new UnsupportedOperationException("");
    }

    @Override
    protected QName createQName(String prefix, String localName) {
        Preconditions.checkArgument(elementNamespace != null, "Failed to lookup prefix %s", prefix);

        final URI ns = URI.create(elementNamespace);
        final Module module = schemaContext.findModuleByNamespaceAndRevision(ns, null);
        Preconditions.checkArgument(module != null, "Namespace %s is not owned by a module", ns);
        return QName.create(module.getQNameModule(), localName);
    }

}
