/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.helpers;

import org.opendaylight.yangtools.yang.data.api.codec.LeafrefCodec;

class LeafrefCodecImpl implements LeafrefCodec<String> {

    @Override
    public String serialize(final Object data) {
        return String.valueOf(data);
    }

    @Override
    public Object deserialize(final String data) {
        return data;
    }

}