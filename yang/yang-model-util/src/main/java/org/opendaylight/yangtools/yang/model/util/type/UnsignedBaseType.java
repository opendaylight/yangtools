/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

abstract class UnsignedBaseType extends RangedBaseType<UnsignedIntegerTypeDefinition>
        implements UnsignedIntegerTypeDefinition {
    UnsignedBaseType(final QName qname, final Number minValue, final Number maxValue) {
        super(qname, minValue, maxValue);
    }

    @Override
    public final UnsignedConstrainedTypeBuilder newConstrainedTypeBuilder(final SchemaPath path) {
        return new UnsignedConstrainedTypeBuilder(this, path);
    }
}
