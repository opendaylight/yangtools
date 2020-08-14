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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public abstract class IRKeyword implements Immutable {
    static final class Qualified extends IRKeyword {
        private final @NonNull String prefix;

        Qualified(final String prefix, final String localName) {
            super(localName);
            this.prefix = requireNonNull(prefix);

        }

        @Override
        public @NonNull String prefix() {
            return prefix;
        }

        @Override
        public String toString() {
            return prefix + ':' + localName();
        }
    }

    static final class Unqualified extends IRKeyword {
        Unqualified(final String localName) {
            super(localName);
        }

        @Override
        public String prefix() {
            return null;
        }

        @Override
        public String toString() {
            return localName();
        }
    }

    private final @NonNull String localName;

    IRKeyword(final String localName) {
        this.localName = requireNonNull(localName);
    }

    public final @NonNull String localName() {
        return localName;
    }

    public abstract @Nullable String prefix();

    @Override
    public abstract String toString();
}
