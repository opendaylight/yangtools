/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Abstract base class for {@link JSONNormalizedNodeStreamWriter} recursion
 * levels which emit a QName-identified node.
 */
abstract class JSONStreamWriterQNameContext extends JSONStreamWriterContext {
    private final QName qname;

    JSONStreamWriterQNameContext(final JSONStreamWriterContext parent, final QName qname, final boolean mandatory) {
        super(parent, mandatory);
        this.qname = requireNonNull(qname);
    }

    /**
     * Returns the node's identifier as a QName.
     *
     * @return QName identifier
     */
    protected final QName getQName() {
        return qname;
    }

    @Nonnull
    @Override
    protected final URI getNamespace() {
        return qname.getNamespace();
    }
}