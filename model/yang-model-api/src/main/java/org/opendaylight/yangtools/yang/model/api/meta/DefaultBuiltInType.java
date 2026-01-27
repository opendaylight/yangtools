/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;

@NonNullByDefault
record DefaultBuiltInType(ArgumentDefinition asTypeArgument) implements BuiltInType {
    DefaultBuiltInType {
        requireNonNull(asTypeArgument);
    }

    DefaultBuiltInType(final String typeName) {
        this(new ArgumentDefinition(QName.create(YangConstants.RFC6020_YANG_MODULE, typeName).intern(), false));
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(BuiltInType.class).add("name", simpleName()).toString();
    }
}
