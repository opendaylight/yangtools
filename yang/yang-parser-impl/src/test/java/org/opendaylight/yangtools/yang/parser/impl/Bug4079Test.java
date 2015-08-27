/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;

public class Bug4079Test {

    private Set<Module> modules;

    @Test
    public void testStringPattern() throws URISyntaxException, IOException {
        modules = TestUtils.loadModules(getClass().getResource("/bugs/bug4079").toURI());
        assertNotNull(modules);
    }
}
