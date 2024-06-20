/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import java.util.Objects;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.Type;

final class ConstantImpl implements Constant {
    private final Type type;
    private final String name;
    private final Object value;

    ConstantImpl(final Type type, final String name, final Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(this.name);
        result = prime * result + Objects.hashCode(this.type);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ConstantImpl other = (ConstantImpl) obj;
        return Objects.equals(this.name, other.name) && Objects.equals(this.type, other.type)
                && Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Constant [type=");
        builder.append(this.type);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", value=");
        builder.append(this.value);
        builder.append("]");
        return builder.toString();
    }
}
