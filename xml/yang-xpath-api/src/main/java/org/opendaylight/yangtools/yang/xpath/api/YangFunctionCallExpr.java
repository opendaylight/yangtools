/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.Serial;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Function call invocation. Function names without a prefix are mapped into {@link YangConstants#RFC6020_YIN_MODULE},
 * as they are required to be mapped into YANG as per RFC7950 definitions.
 */
public sealed class YangFunctionCallExpr implements YangExpr {
    private static final class WithArgs extends YangFunctionCallExpr {
        private static final long serialVersionUID = 1L;

        private final ImmutableList<YangExpr> arguments;

        WithArgs(final QName name, final List<YangExpr> arguments) {
            super(name);
            this.arguments = ImmutableList.copyOf(arguments);
        }

        @Override
        public List<YangExpr> getArguments() {
            return arguments;
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private final QName name;

    private YangFunctionCallExpr(final QName name) {
        this.name = requireNonNull(name);
    }

    public static YangFunctionCallExpr of(final QName name) {
        return new YangFunctionCallExpr(name);
    }

    public static YangFunctionCallExpr of(final QName name, final YangExpr argument) {
        return new WithArgs(name, ImmutableList.of(argument));
    }

    public static YangFunctionCallExpr of(final QName name, final List<YangExpr> arguments) {
        return arguments.isEmpty() ? of(name) : new WithArgs(name, arguments);
    }

    public final QName getName() {
        return name;
    }

    public List<YangExpr> getArguments() {
        return ImmutableList.of();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getName(), getArguments());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangFunctionCallExpr other && getName().equals(other.getName())
            && getArguments().equals(other.getArguments());
    }

    @Override
    public final String toString() {
        final var sb = new StringBuilder().append(getName()).append('(');
        final var it = getArguments().iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(", ").append(it.next());
            }
        }
        return sb.append(')').toString();
    }
}
