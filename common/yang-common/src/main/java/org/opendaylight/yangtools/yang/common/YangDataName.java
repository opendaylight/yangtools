/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Identifier of a RESTCONF {@code yang-data} extension template instantiation. The {@link #module()} method returns
 * the namespace and revision of use and {@link #name()} returns the name of the template.
 */
public record YangDataName(@NonNull QNameModule module, @NonNull String name) implements Identifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final Interner<@NonNull YangDataName> INTERNER = Interners.newWeakInterner();

    public YangDataName {
        requireNonNull(module);
        checkArgument(!name.isEmpty(), "name must not be empty");
    }

    /**
     * Intern this instance.
     *
     * @return An interned instance.
     */
    public @NonNull YangDataName intern() {
        return INTERNER.intern(this);
    }
}
