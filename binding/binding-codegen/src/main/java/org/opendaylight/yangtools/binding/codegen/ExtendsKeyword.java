/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link BlockFragment} emitting an {@code extends} keyword followed by one or more more {@link TypeReference}s.
 */
@NonNullByDefault
record ExtendsKeyword(TypeReference first, Stream<TypeReference> others) implements BlockFragment {
    ExtendsKeyword {
        requireNonNull(first);
        requireNonNull(others);
    }

    static ExtendsKeyword of(final TypeReference first, final Stream<TypeReference> others) {
        return new ExtendsKeyword(first, others);
    }

    static ExtendsKeyword of(final TypeReference first, final TypeReference second,
            final Stream<TypeReference> others) {
        return new ExtendsKeyword(first, Stream.concat(Stream.of(requireNonNull(second)), others));
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        // We can have three shapes here to ensure reasonable separation from inner members:
        //
        //   interface Foo extends One {
        //       int VALUE = 42;
        //
        // or
        //
        //   interface Foo
        //       extends One,
        //               Two {
        //       int VALUE = 42;
        //
        final var it = others.iterator();
        if (!it.hasNext()) {
            bb.str(" extends ").frg(first);
            return;
        }

        // Note: We could try to pack multiple references into a single line, but that would require us to pick a length
        //       limit and peek into importedName to see how long it is.
        //       Perhaps it is worth the added complexity: for now this simple approach just works
        bb.nl().ind("extends ").frg(first);
        do {
            // space equivalent of 'extends'
            bb.eol(",").ind("        ").frg(it.next());
        } while (it.hasNext());
    }
}
