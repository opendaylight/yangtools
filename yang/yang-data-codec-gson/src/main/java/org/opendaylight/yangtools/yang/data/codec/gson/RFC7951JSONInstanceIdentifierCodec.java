/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class RFC7951JSONInstanceIdentifierCodec extends JSONInstanceIdentifierCodec {
    RFC7951JSONInstanceIdentifierCodec(final SchemaContext context, final JSONCodecFactory jsonCodecFactory) {
        super(context, jsonCodecFactory);
    }

    @Override
    protected QName createQName(final QNameModule lastModule, final String maybePrefix) {
        checkArgument(lastModule != null, "Unprefixed leading name %s", maybePrefix);
        return QName.create(lastModule, maybePrefix);
    }
}
