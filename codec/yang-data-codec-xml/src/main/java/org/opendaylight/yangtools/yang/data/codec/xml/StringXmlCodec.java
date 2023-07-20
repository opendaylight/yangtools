/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;

@Deprecated
final class StringXmlCodec extends QuotedXmlCodec<String> {
    StringXmlCodec(final StringStringCodec codec) {
        super(codec);
    }

    @Override
    @Deprecated
    String trimValue(final String str) {
        return str;
    }
}
