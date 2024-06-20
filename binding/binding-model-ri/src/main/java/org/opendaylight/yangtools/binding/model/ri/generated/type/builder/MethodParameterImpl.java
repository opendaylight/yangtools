/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.Objects;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.Parameter;
import org.opendaylight.yangtools.binding.model.api.Type;

final class MethodParameterImpl implements Parameter {

    private final String name;
    private final Type type;

    MethodParameterImpl(final String name, final Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(this.name);
        result = prime * result + Objects.hashCode(this.type);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MethodParameterImpl other = (MethodParameterImpl) obj;
        return Objects.equals(this.name, other.name) && Objects.equals(this.type, other.type);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MethodParameter [name=");
        builder.append(this.name);
        builder.append(", type=");
        builder.append(this.type.getPackageName());
        builder.append(".");
        builder.append(this.type.getName());
        builder.append("]");
        return builder.toString();
    }
}
