/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype.Pair;
import org.opendaylight.yangtools.yang.model.api.Status;

/**
 * Base class for {@link Pair} implementations.
 */
public abstract sealed class AbstractPair implements Pair {
    static final class CodegenPair extends AbstractPair {
        private final @NonNull Status status;
        private final String description;
        private final String reference;

        CodegenPair(final String name, final String mappedName, final int value, final Status status,
                final String description, final String reference) {
            super(name, mappedName, value);
            this.status = requireNonNull(status);
            this.description = description;
            this.reference = reference;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public Optional<String> getReference() {
            return Optional.ofNullable(reference);
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }

    static final class RuntimePair extends AbstractPair {
        RuntimePair(final String name, final String mappedName, final int value) {
            super(name, mappedName, value);
        }

        @Override
        public Optional<String> getDescription() {
            throw uoe();
        }

        @Override
        public Optional<String> getReference() {
            throw uoe();
        }

        @Override
        public Status getStatus() {
            throw uoe();
        }

        private static UnsupportedOperationException uoe() {
            return new UnsupportedOperationException("Not available at runtime");
        }
    }

    private final @NonNull String name;
    private final @NonNull String mappedName;
    private final int value;

    AbstractPair(final String name, final String mappedName, final int value) {
        this.name = requireNonNull(name);
        this.mappedName = requireNonNull(mappedName);
        this.value = value;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getMappedName() {
        return mappedName;
    }

    @Override
    public final int getValue() {
        return value;
    }

    @Override
    public final int hashCode() {
        return name.hashCode() * 31 + value;
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj == this || obj instanceof AbstractPair other && name.equals(other.name) && value == other.value;
    }

    @Override
    public final String toString() {
        return new StringBuilder().append("EnumPair [name=").append(name)
            .append(", mappedName=").append(mappedName)
            .append(", value=").append(value)
            .append("]").toString();
    }
}