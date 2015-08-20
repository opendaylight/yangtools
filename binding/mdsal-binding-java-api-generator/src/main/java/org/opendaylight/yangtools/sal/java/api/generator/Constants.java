/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

/**
 * 
 * Various constants when generating JAVA source code.
 * 
 */
final class Constants {

    public static final String COMMA = ",";
    public static final String DOT = ".";

    /**
     * Name of the class constant which contains list of <code>Pattern</code>
     * instances.
     */
    public static final String MEMBER_PATTERN_LIST = "patterns";

    /**
     * It doesn't have the sense to create the instances of this class.
     */
    private Constants() {
    }
}
