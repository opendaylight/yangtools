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

/**
 * Name of an individual YANG annotation, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc7952#section-5.2.1">RFC7952</a>.
 */
@NonNullByDefault
public record AnnotationName(QName qname) implements Identifier, WritableObject {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final Interner<AnnotationName> INTERNER = Interners.newWeakInterner();

    public AnnotationName {
        requireNonNull(qname);
    }

    public AnnotationName intern() {
        final var cacheQName = qname.intern();

        // Identity comparison is here on purpose, as we are deciding whether to potentially store 'qname'. It is
        // important that it does not hold user-supplied reference (such a String instance from parsing of an XML
        // document).
        final var template = cacheQName == qname ? this : new AnnotationName(cacheQName);

        return INTERNER.intern(template);
    }

    public static AnnotationName readFrom(final DataInput in) throws IOException {
        return new AnnotationName(QName.readFrom(in));
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        qname.writeTo(out);
    }
}
