/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.net.URI;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.util.AbstractStringInstanceIdentifierCodec;

public class ElementContentInstanceIdentifierParser extends AbstractStringInstanceIdentifierCodec {

    @Override
    protected String prefixForNamespace(URI namespace) {
        throw new UnsupportedOperationException("prfixForNamespace not supported!!!");
    }

    @Override
    protected QName createQName(String prefix, String localName) {
        throw new UnsupportedOperationException("createQName not supported!!!");
    }
}
