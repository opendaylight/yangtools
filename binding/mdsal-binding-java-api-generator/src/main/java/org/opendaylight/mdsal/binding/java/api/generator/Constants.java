/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

/**
 *
 * Various constants when generating JAVA source code.
 *
 */
final class Constants {

    public static final String COMMA = ",";
    public static final String DOT = ".";

    /**
     * Name of the class constant which contains list of <code>Pattern</code> instances. The type of this constant
     * is Pattern[] for more than one pattern, or Pattern if there is only a single one.
     */
    public static final String MEMBER_PATTERN_LIST = "patterns";

    /**
     * Name of the class constant which contains a list of XSD regular expression strings. The type of this constant
     * is String[] (or String for single strings) and it corresponds to {@link #MEMBER_PATTERN_LIST} in both size and
     * ordering.
     */
    public static final String MEMBER_REGEX_LIST = "regexes";

    /**
     * It doesn't have the sense to create the instances of this class.
     */
    private Constants() {
    }
}
