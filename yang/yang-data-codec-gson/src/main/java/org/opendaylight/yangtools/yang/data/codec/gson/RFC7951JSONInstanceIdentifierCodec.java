/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class RFC7951JSONInstanceIdentifierCodec extends JSONInstanceIdentifierCodec {
    RFC7951JSONInstanceIdentifierCodec(final SchemaContext context, final JSONCodecFactory jsonCodecFactory) {
        super(context, jsonCodecFactory);
    }

    @Override
    protected StringBuilder appendQName(final StringBuilder sb, final QName qname,
            final @Nullable QNameModule lastModule) {
        if (qname.getModule().equals(lastModule)) {
            return sb.append(qname.getLocalName());
        }

        return super.appendQName(sb, qname, lastModule);
    }

    @Override
    protected QName createQName(final @Nonnull QNameModule lastModule, final String localName) {
        checkArgument(lastModule != null, "Unprefixed leading name %s", localName);
        return QName.create(lastModule, localName);
    }
}
