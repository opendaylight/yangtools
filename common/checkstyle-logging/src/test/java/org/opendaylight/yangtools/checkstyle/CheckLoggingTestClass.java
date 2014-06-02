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
    private static final java.util.logging.Logger LOG2 = java.util.logging.Logger.getGlobal();
    private static final Logger LOG = LoggerFactory.getLogger(CheckLoggingTestClass.class);
    public void foo() {
        try {
            logger.debug("foo + bar {}", "foo");
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.err.print(e.getMessage());
            logger.debug("foo {}", "bar", e);
            LOG.info("foo {} {}", e.getMessage(), e);
        }
    }

    public void bar(String string, IllegalArgumentException e) {
        LOG.warn("foo", e);
        LOG.warn("foo {}", e);
        LOG.warn("foo", string);
        LOG.warn("foo {}", string);
        LOG.warn("foo {}", string, e);
    }
}
