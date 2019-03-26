/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.IdentityrefCodec;

/**
 * Abstract utility class for representations which encode Identityref as a
 * prefix:name tuple. Typical uses are RESTCONF/JSON (module:name) and XML (prefix:name).
 */
@Beta
public abstract class AbstractStringIdentityrefCodec extends AbstractNamespaceCodec
        implements IdentityrefCodec<String> {
    @Override
    public String serialize(final QName data) {
        return appendQName(new StringBuilder(), data).toString();
    }

    @Override
    public QName deserialize(final String data) {
        return parseQName(data);
    }
}
