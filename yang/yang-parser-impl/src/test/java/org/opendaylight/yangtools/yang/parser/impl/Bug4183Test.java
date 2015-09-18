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
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

public class Bug4183Test {

    Set<Module> modules;

    @Before
    public void setup() throws URISyntaxException, IOException {
        modules = TestUtils.loadModules(getClass().getResource("/bugs/bug4183").toURI());
    }

    @Test
    public void testAugmentedShortCase() {
        final Module module = TestUtils.findModule(modules, "uses-augment-shortcase-test");
        assertNotNull(module);

        final ContainerSchemaNode cont = (ContainerSchemaNode) module.getDataChildByName("cont");
        final ChoiceSchemaNode mychoice = (ChoiceSchemaNode) cont.getDataChildByName("mychoice");
        assertNotNull(mychoice.getCaseNodeByName("myleaf1Case"));
    }
}
