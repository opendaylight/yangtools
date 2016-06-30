/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.IndexKey;

public final class UniqueIndexKey implements IndexKey<Object> {
    private final List<Object> values;

    public UniqueIndexKey(final List<?> values) {
        Preconditions.checkNotNull(values);
        this.values = ImmutableList.copyOf(values);
    }

    @Override
    public Collection<Object> getValues() {
        return values;
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
        return Objects.equals(this.values, other.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
