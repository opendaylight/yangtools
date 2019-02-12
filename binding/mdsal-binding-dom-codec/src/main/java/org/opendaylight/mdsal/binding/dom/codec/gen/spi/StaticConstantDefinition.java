/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.spi;

import static java.util.Objects.requireNonNull;

/**
 * Definition of static property for generated class.
 *
 * <p>
 * This definition consists of
 * <ul>
 * <li>name - property name</li>
 * <li>type - Java type for property</li>
 * <li>value - value to which property should be initialized</li>
 * </ul>
 */
public class StaticConstantDefinition {

    private final String name;
    private final Class<?> type;
    private final Object value;

    public StaticConstantDefinition(final String name, final Class<?> type, final Object value) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
        this.value = requireNonNull(value);
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StaticConstantDefinition other = (StaticConstantDefinition) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
