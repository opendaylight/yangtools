/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

abstract class AbstractRestrictedTypeBuilder<T extends TypeDefinition<T>> extends TypeBuilder<T> {
    private boolean touched;

    AbstractRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);
        if (baseType != null) {
            Preconditions.checkArgument(baseType instanceof AbstractBaseType || baseType instanceof AbstractDerivedType,
                "Restricted type has to be based on either a base or derived type, not %s", baseType);
        } else {
            touched = true;
        }
    }

    final void touch() {
        touched = true;
    }

    abstract @NonNull T buildType();

    @Override
    public final T build() {
        if (!touched) {
            return getBaseType();
        }

        return buildType();
    }
}
