/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.Writer;

/**
 * A virtual recursion level in {@link JSONNormalizedNodeStreamWriter}, used for nodes which are not emitted in the JSON
 * representation.
 */
final class JSONStreamWriterInvisibleContext extends JSONStreamWriterURIContext {
    boolean firstChild = true;

    JSONStreamWriterInvisibleContext(final JSONStreamWriterContext parent) {
        super(Preconditions.checkNotNull(parent), parent.getNamespace(), parent.getIndentLevel());
    }

    @Override
    protected void emitEnd(final Writer writer, final String indent) {
        // No-op
    }

    /**
     * White spaces (new line + indentation) is for invisible node printed only if leafNode, leafSetEntryNode,
     * anyXmlNode methods are called in context of invisible node (it means that doesn't exist context which parent
     * would be this invisible context) and it isn't first child of this node (because this indentation is prepared by
     * parent context).
     */
    @Override
    protected void writeWhiteSpaces(final Writer writer, final String indent) throws IOException {
        if (!hasChildContext() && !firstChild) {
            super.writeWhiteSpaces(writer, indent);
        }
        firstChild = false;
    }

}