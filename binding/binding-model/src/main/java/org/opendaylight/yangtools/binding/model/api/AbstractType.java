/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * It is used only as ancestor for other <code>Type</code>s. Note this forms the equality domain over most types, please
 * consider joining the party.
 */
@Beta
public abstract class AbstractType implements Type {
    @Override
    public final int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof Type type && getIdentifier().equals(type.getIdentifier());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("identifier", getIdentifier());
    }
}
