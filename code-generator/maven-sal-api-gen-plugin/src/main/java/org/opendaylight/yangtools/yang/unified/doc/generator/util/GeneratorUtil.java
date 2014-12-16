/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.unified.doc.generator.util;

import com.google.common.collect.Iterables;


public final class GeneratorUtil {

    public static final String TAB = "    ";
    public static final char NEW_LINE = Character.LINE_SEPARATOR;
    public static final String EMPTY_STRING = "";

    public static String indentWithNewLine(final int numOfTab, final String text) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < numOfTab; i++) {
            sb.append(TAB);
        }
        sb.append(text);
        sb.append(NEW_LINE);

        return sb.toString();
    }

    public static String concat(final String... strings) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    public static <T> Iterable<T> filter(Iterable<? super T> unfiltered, Class<T> type) {
        return Iterables.filter(unfiltered, type);
    }
}
