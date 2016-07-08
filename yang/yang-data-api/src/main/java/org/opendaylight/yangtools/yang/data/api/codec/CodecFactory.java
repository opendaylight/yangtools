/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public interface CodecFactory<T extends Codec<String, ?>> {
    public T codecForIdentityref(final DataSchemaNode schema);
}
