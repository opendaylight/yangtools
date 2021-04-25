/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class Lhotka02JSONInstanceIdentifierCodec extends JSONInstanceIdentifierCodec {
    Lhotka02JSONInstanceIdentifierCodec(final EffectiveModelContext context, final JSONCodecFactory jsonCodecFactory) {
        super(context, jsonCodecFactory);
    }
}
