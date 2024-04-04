/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.codec.AbstractIllegalArgumentCodec;
import org.opendaylight.yangtools.yang.data.util.codec.DataStringCodec;

// FIXME: 7.0.0: yang-data-api is tied to yang-model-api, hence it should be opinionated to export exceptions
//               encapsulating YANG-based error information.
@Beta
public abstract class AbstractDataStringCodec<T> extends AbstractIllegalArgumentCodec<String, T>
        implements DataStringCodec<T> {

    // FIXME: should we provide default getInputClass() implementation?
}
