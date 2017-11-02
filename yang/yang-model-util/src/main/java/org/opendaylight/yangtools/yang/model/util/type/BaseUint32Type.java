/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseUint32Type extends AbstractUnsignedBaseType<Long, Uint32TypeDefinition>
        implements Uint32TypeDefinition {
    static final BaseUint32Type INSTANCE = new BaseUint32Type();

    private BaseUint32Type() {
        super(BaseTypes.UINT32_QNAME, 0L, 4294967295L);
    }
}
