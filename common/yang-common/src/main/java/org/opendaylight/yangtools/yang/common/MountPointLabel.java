/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.WritableObject;

@NonNullByDefault
public record MountPointLabel(QName qname) implements Identifier, WritableObject {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final Interner<MountPointLabel> INTERNER = Interners.newWeakInterner();

    public MountPointLabel {
        requireNonNull(qname);
    }

    public MountPointLabel intern() {
        final var cacheQName = qname.intern();

        // Identity comparison is here on purpose, as we are deciding whether to potentially store 'qname'. It is
        // important that it does not hold user-supplied reference (such a String instance from parsing of an XML
        // document).
        final var template = cacheQName == qname ? this : new MountPointLabel(cacheQName);

        return INTERNER.intern(template);
    }

    public static MountPointLabel readFrom(final DataInput in) throws IOException {
        return new MountPointLabel(QName.readFrom(in));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        qname.writeTo(out);
    }
}
