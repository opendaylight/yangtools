/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class ControllerTest {

    private final static List<File> controllerModels = new ArrayList<>();
    private final static URL controllerModelsFolderPath = ControllerTest.class
            .getResource("/controller-models");

    @BeforeClass
    public static void loadTestResources() throws URISyntaxException {
        final File ctrlFolder = new File(controllerModelsFolderPath.toURI());

        for (final File fileEntry : ctrlFolder.listFiles()) {
            if (fileEntry.isFile()) {
                controllerModels.add(fileEntry);
            }
        }
    }

    @Test
    public void controllerAugmentationTest() {
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(controllerModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertNotNull(genTypes);
        assertTrue(!genTypes.isEmpty());
    }
}
