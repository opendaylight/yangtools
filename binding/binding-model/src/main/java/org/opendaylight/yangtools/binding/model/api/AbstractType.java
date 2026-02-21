/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * It is used only as ancestor for other <code>Type</code>s. Note this forms the equality domain over most types, please
 * consider joining the party.
 */
@Beta
@NonNullByDefault
public abstract class AbstractType implements Type {
    private final JavaTypeName name;

    /**
     * Constructs the instance of this class with a JavaTypeName.
     *
     * @param name name of this type
     */
    protected AbstractType(final JavaTypeName name) {
        this.name = requireNonNull(name);
    }

    @Override
    public final JavaTypeName name() {
        return name;
    }

    @Override
    public final int hashCode() {
        return name().hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type type && name().equals(type.name());
    }


    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("name", name);
    }

    protected static final void addToStringAttribute(final ToStringHelper helper, final String name,
            final @Nullable Collection<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }
}
