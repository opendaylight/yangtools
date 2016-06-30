/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.IndexKey;

public final class UniqueIndexKey implements IndexKey<Object> {
    private final Map<YangInstanceIdentifier, Object> value;

    public UniqueIndexKey(final Map<YangInstanceIdentifier, ?> value) {
        Preconditions.checkNotNull(value);
        this.value = ImmutableMap.copyOf(value);
    }

    @Override
    public Map<YangInstanceIdentifier, Object> getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UniqueIndexKey)) {
            return false;
        }
        final UniqueIndexKey other = (UniqueIndexKey) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
