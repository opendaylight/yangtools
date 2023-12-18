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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * Identifier of a RESTCONF {@code yang-data} extension template instantiation.
 *
 * @param module namespace of defining module
 * @param name template name
 */
@NonNullByDefault
public record YangDataName(QNameModule module, String name)
        implements Comparable<YangDataName>, Identifier, WritableObject {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final Interner<YangDataName> INTERNER = Interners.newWeakInterner();

    public YangDataName {
        requireNonNull(module);
        checkArgument(!name.isEmpty(), "name must not be empty");
    }

    /**
     * Intern this instance.
     *
     * @return An interned instance.
     */
    public YangDataName intern() {
        final var cacheMod = module.intern();

        // Identity comparison is here on purpose, as we are deciding whether to potentially store 'module' into the
        // interner. It is important that it does not hold user-supplied reference (such a String instance from
        // parsing of an XML document).
        final var template = cacheMod == module ? this : new YangDataName(cacheMod, name.intern());

        return INTERNER.intern(template);
    }

    public static YangDataName readFrom(final DataInput in) throws IOException {
        return new YangDataName(QNameModule.readFrom(in), in.readUTF());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        module.writeTo(out);
        out.writeUTF(name);
    }

    @Override
    public int compareTo(final YangDataName o) {
        var cmp = name.compareTo(o.name);
        return cmp != 0 ? cmp : module.compareTo(o.module);
    }
}
