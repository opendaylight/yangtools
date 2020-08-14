/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public abstract class IRArgument implements Immutable {
    public static final class Concatenation extends IRArgument {
        private final ImmutableList<IRArgument> parts;

        Concatenation(final ImmutableList<IRArgument> parts) {
            this.parts = requireNonNull(parts);
        }

        public @NonNull ImmutableList<IRArgument> parts() {
            return parts;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            final Iterator<IRArgument> it = parts.iterator();
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(" + ").append(it.next());
            }
            return sb.toString();
        }
    }

    public static final class DoubleQuoted extends AbstractSimple {
        DoubleQuoted(final String string) {
            super(string);
        }

        @Override
        public String toString() {
            return '"' + string() + '"';
        }
    }

    public abstract static class Literal extends AbstractSimple {
        Literal(final String string) {
            super(string);
        }
    }

    private abstract static class AbstractSimple extends IRArgument {
        private final @NonNull String string;

        AbstractSimple(final String string) {
            this.string = requireNonNull(string);
        }

        public final @NonNull String string() {
            return string;
        }
    }

    static final class SingleQuoted extends Literal {
        SingleQuoted(final String string) {
            super(string);
        }

        @Override
        public String toString() {
            return "'" + string() + "'";
        }
    }

    static final class Unquoted extends Literal {
        Unquoted(final String string) {
            super(string);
        }

        @Override
        public String toString() {
            return string();
        }
    }

    IRArgument() {
        // Hidden on purpose
    }

    @Override
    public abstract String toString();
}
