/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import java.math.BigDecimal;
import org.opendaylight.yangtools.concepts.Codec;

public interface DecimalCodec<T>  extends Codec<T,BigDecimal> {
    @Override
    T serialize(BigDecimal data);

    @Override
    BigDecimal deserialize(T data);
}
