/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import java.io.IOException;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;

// FIXME: YANGTOOLS-961: remove this class
@Deprecated
public abstract class ForwardingNormalizedNodeStreamAttributeWriter extends ForwardingNormalizedNodeStreamWriter
        implements NormalizedNodeStreamAttributeWriter {
    @Override
    protected abstract NormalizedNodeStreamAttributeWriter delegate();

    @Override
    public void attributes(final Map<QName, String> attributes) throws IOException {
        delegate().attributes(attributes);
    }
}
