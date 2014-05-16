/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckLoggingTestClass {
    final Logger logger = LoggerFactory.getLogger(CheckstyleTest.class);
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getGlobal();

    public void fooMethod() {
        try {
            logger.debug("foo + bar {}", "foo");
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.err.print(e.getMessage());
            logger.debug("foo {}", "bar", e);
            logger.info("foo {} {}", e.getMessage(), e);
        }
    }
}