/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.IndexKey;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableIndexKey;

final class MutableUniqueIndexKey implements MutableIndexKey {
    private final List<Object> mutableValues;

    MutableUniqueIndexKey(final List<?> values) {
        Preconditions.checkNotNull(values);
        this.mutableValues = new ArrayList<>(values);
    }

    @Override
    public void add(final Object value) {
        this.mutableValues.add(value);
    }

    @Override
    public void clear() {
        this.mutableValues.clear();
    }

    @Override
    public Collection<?> getValues() {
        return mutableValues;
    }

    @Override
    public IndexKey seal() {
        return new UniqueIndexKey(mutableValues);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MutableUniqueIndexKey)) {
            return false;
        }
        final MutableUniqueIndexKey other = (MutableUniqueIndexKey) obj;
        return Objects.equals(this.mutableValues, other.mutableValues);
    }

    @Override
    public int hashCode() {
        return mutableValues.hashCode();
    }

}
