/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.codec.IllegalArgumentCodec;

// FIXME: 7.0.0: yang-data-api is tied to yang-model-api, hence it should be opinionated to export exceptions
//               encapsulating YANG-based error information.
public interface DataStringCodec<T> extends IllegalArgumentCodec<String, T> {

    @NonNull Class<T> getInputClass();
}
