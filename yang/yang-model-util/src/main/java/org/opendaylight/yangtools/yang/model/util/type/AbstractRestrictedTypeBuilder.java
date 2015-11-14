/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

abstract class AbstractRestrictedTypeBuilder<T extends TypeDefinition<T>> extends TypeBuilder<T> {
    private boolean touched;

    AbstractRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);
        touched = baseType == null;
    }

    final void touch() {
        touched = true;
    }

    abstract T buildType();

    @Override
    public final T build() {
        if (!touched) {
            return getBaseType();
        }

        return buildType();
    }
}
