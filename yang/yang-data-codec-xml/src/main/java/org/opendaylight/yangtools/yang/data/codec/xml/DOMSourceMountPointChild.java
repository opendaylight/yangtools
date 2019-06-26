/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Iterator;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNormalizationException;
import org.opendaylight.yangtools.yang.data.util.MountPointChild;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Internal MountPointChild implementation, reusing data bits from {@link DOMSourceAnydata}.
 */
@NonNullByDefault
final class DOMSourceMountPointChild extends DOMSourceAnydata implements MountPointChild {
    private final URI namespace;
    private final String localName;

    DOMSourceMountPointChild(final URI namespace, final String localName, final DOMSource source) {
        super(source);
        this.namespace = requireNonNull(namespace);
        this.localName = requireNonNull(localName);
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public QNameModule getNamespace(final SchemaContext schemaContext) throws AnydataNormalizationException {
        final Iterator<Module> it = schemaContext.findModules(namespace).iterator();
        if (!it.hasNext()) {
            throw new AnydataNormalizationException("Failed to find module for namespace " + namespace);
        }
        return it.next().getQNameModule();
    }
}
