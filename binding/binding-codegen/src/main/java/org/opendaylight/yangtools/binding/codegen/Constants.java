/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

/**
 * Various constants when generating JAVA source code.
 */
final class Constants {
    /**
     * Name or prefix (multiple patterns in builder class as composed with '_' and upper case of the field name)
     * of the class constant which contains list of <code>Pattern</code> instances. The type of this constant is
     * Pattern[] for more than one pattern, or Pattern if there is only a single one.
     */
    static final String MEMBER_PATTERN_LIST = "patterns";

    /**
     * Name or prefix (multiple patterns in builder class as composed with '_' and upper case of the field name)
     * of the class constant which contains a list of XSD regular expression strings. The type of this constant is
     * String[] (or String for single strings) and it corresponds to {@link #MEMBER_PATTERN_LIST} in both size
     * and ordering.
     */
    static final String MEMBER_REGEX_LIST = "regexes";

    private Constants() {
        // Hidden on purpose
    }
}
