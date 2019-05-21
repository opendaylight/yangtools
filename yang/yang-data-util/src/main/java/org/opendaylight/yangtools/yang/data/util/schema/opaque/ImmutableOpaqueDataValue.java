/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.schema.opaque;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataValue;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier;

@NonNullByDefault
final class ImmutableOpaqueDataValue extends AbstractOpaqueDataNode implements OpaqueDataValue {
    private final Object value;

    ImmutableOpaqueDataValue(final OpaqueIdentifier identifier, final Object value) {
        super(identifier);
        this.value = requireNonNull(value);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return 31 + getIdentifier().hashCode() + value.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OpaqueDataValue)) {
            return false;
        }
        final OpaqueDataValue other = (OpaqueDataValue) obj;
        return getIdentifier().equals(other.getIdentifier()) && value.equals(other.getValue());
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("value", value);
    }
}
