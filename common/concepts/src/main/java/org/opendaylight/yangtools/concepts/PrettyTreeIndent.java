/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indentation handling for {@link PrettyTree}. This class is split out to defer initialization of the string table --
 * it might never be used after all.
 *
 * <p>We want to be formatting strings quickly and a lot of that can very easily be dominated by dealing with indents.
 * To deal with that we pre-compute a few indentation strings and then append them directly using a specialized
 * method. We allow tuning the default indentation at runtime, but choose a fixed string table size.
 */
final class PrettyTreeIndent {
    private static final Logger LOG = LoggerFactory.getLogger(PrettyTreeIndent.class);
    private static final int DEFAULT_INDENT = 4;
    /**
     * Size of indent table. The idea is to strike a balance between memory footprint and the number of operations we
     * perform in {@link #indent(StringBuilder, int)}. Indentation up to {@value #INDENT_STRINGS_SIZE} result in a
     * single operation.
     */
    private static final int INDENT_STRINGS_SIZE = 16;
    private static final String[] INDENT_STRINGS;

    static {
        int indent = Integer.getInteger("org.opendaylight.yangtools.concepts.pretty-tree-indent", DEFAULT_INDENT);
        if (indent < 1) {
            LOG.warn("Invalid pretty-tree-indent {}, using {} instead", indent, DEFAULT_INDENT);
            indent = DEFAULT_INDENT;
        } else if (indent != DEFAULT_INDENT) {
            LOG.info("Using pretty-tree-indent {}", indent);
        }

        final String one = " ".repeat(indent);
        final String[] strings = new String[INDENT_STRINGS_SIZE];
        for (int i = 0; i < INDENT_STRINGS_SIZE; i++) {
            strings[i] = one.repeat(i).intern();
        }
        INDENT_STRINGS = strings;
    }

    private PrettyTreeIndent() {
        // Hidden on purpose
    }

    static void indent(final StringBuilder sb, final int depth) {
        int remaining = depth;
        while (remaining >= INDENT_STRINGS_SIZE) {
            sb.append(INDENT_STRINGS[INDENT_STRINGS_SIZE - 1]);
            remaining -= INDENT_STRINGS_SIZE;
        }
        sb.append(INDENT_STRINGS[remaining]);
    }

    static void indent(final Appendable appendable, final int depth) throws IOException {
        int remaining = depth;
        while (remaining >= INDENT_STRINGS_SIZE) {
            appendable.append(INDENT_STRINGS[INDENT_STRINGS_SIZE - 1]);
            remaining -= INDENT_STRINGS_SIZE;
        }
        appendable.append(INDENT_STRINGS[remaining]);
    }
}
