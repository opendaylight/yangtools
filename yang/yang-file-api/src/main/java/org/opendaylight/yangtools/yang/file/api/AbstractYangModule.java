/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.file.api;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * Abstract base class for {@link YangModule} implementations.
 */
@Beta
public abstract class AbstractYangModule implements YangModule {
    @Override
    public final int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj != null && getClass().equals(obj.getClass())
                && getIdentifier().equals(((AbstractYangModule) obj).getIdentifier());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("id", getIdentifier());
    }
}
