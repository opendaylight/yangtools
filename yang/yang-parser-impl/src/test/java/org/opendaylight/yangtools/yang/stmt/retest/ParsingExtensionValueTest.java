/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Test for testing of extensions and their arguments.
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class ParsingExtensionValueTest {

    private Set<Module> modules;

    @Before
    public void init() throws Exception {
        //TODO: change test or create new module in order to respect new statement parser validations
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            modules = TestUtils.loadModules(getClass().getResource("/extensions").toURI());
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertTrue(e.getMessage().startsWith("ext:id is not a YANG statement or use of extension"));
        }
    }

    @Test
    public void parsingExtensionArgsTest() {

    }
}
