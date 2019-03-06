/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

abstract class AbstractPathArgument implements PathArgument {
    private static final long serialVersionUID = -4546547994250849340L;

    private final QName nodeType;

    private transient int hashValue;
    private transient volatile boolean hashGuard = false;

    AbstractPathArgument(final QName nodeType) {
        this.nodeType = requireNonNull(nodeType);
    }

    @Override
    public final QName getNodeType() {
        return nodeType;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(@Nonnull final PathArgument o) {
        return nodeType.compareTo(o.getNodeType());
    }

    protected int hashCodeImpl() {
        return 31 + getNodeType().hashCode();
    }

    @Override
    public final int hashCode() {
        if (!hashGuard) {
            hashValue = hashCodeImpl();
            hashGuard = true;
        }

        return hashValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        return getNodeType().equals(((AbstractPathArgument)obj).getNodeType());
    }

    @Override
    public String toString() {
        return getNodeType().toString();
    }

    @Override
    public String toRelativeString(final PathArgument previous) {
        if (previous instanceof AbstractPathArgument) {
            final QNameModule mod = previous.getNodeType().getModule();
            if (getNodeType().getModule().equals(mod)) {
                return getNodeType().getLocalName();
            }
        }

        return getNodeType().toString();
    }

    static String computeToString(final List<PathArgument> pathArguments) {
        final StringBuilder builder = new StringBuilder("/");
        PathArgument prev = null;
        for (PathArgument argument : pathArguments) {
            if (prev != null) {
                builder.append('/');
            }
            builder.append(argument.toRelativeString(prev));
            prev = argument;
        }

        return builder.toString();
    }

    static int valueHashCode(final Object value) {
        if (value == null) {
            return 0;
        }

        if (byte[].class.equals(value.getClass())) {
            return Arrays.hashCode((byte[]) value);
        }

        if (value.getClass().isArray()) {
            int hash = 0;
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                hash += Objects.hashCode(Array.get(value, i));
            }

            return hash;
        }

        return Objects.hashCode(value);
    }
}