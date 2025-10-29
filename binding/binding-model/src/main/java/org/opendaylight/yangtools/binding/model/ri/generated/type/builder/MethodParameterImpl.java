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
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(type);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof MethodParameterImpl other &&
            Objects.equals(name, other.name) && Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("MethodParameter [name=").append(name)
            .append(", type=").append(type.getPackageName()).append('.').append(type.getName())
            .append(']').toString();
    }
}
