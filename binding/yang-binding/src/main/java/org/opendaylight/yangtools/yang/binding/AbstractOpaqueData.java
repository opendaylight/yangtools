/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility class for {@link OpaqueData} implementations. This class provides baseline implementation of
 * {@link #hashCode()} and {@link #equals(Object)} as specified by {@link OpaqueData}. For cases where the object
 * model's objects do not provide a usable implementation of hashCode/equals, this class is expected to be subclassed
 * to provide alternative implementation of {@link #dataHashCode()} and {@link #dataEquals(Object)} methods. Such
 * class should be made public in a convenient place. Note such customized methods are required to maintain consistency
 * between hashCode and equals, as well as the <i>reflexive</i>, <i>symmetric</i>, <i>transitive</i> and
 * <i>consistent</i> properties as detailed in {@link Object#equals(Object)}.
 *
 * @param <T> Data object model type
 */
@Beta
public abstract class AbstractOpaqueData<T> implements OpaqueData<T> {
    @Override
    public final int hashCode() {
        return 31 * getObjectModel().hashCode() + dataHashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpaqueData)) {
            return false;
        }
        final OpaqueData<?> other = (OpaqueData<?>) obj;
        return getObjectModel().equals(other.getObjectModel()) && dataEquals(other.getData());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("objectModel", getObjectModel())).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("data", getData());
    }

    /**
     * Determine hashCode of the data. The default implementation uses the data object's {@code hashCode} method.
     *
     * @return Hash code value of data
     */
    protected int dataHashCode() {
        return getData().hashCode();
    }

    protected boolean dataEquals(final @NonNull Object otherData) {
        return getData().equals(otherData);
    }
}
