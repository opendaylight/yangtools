/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileFeatureStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource
                ("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(10, modules.size());
    }

    @Test
    public void testFeature() {
        Module testModule = TestUtils.findModule(modules, "ietf-interfaces");
        assertNotNull(testModule);

        Set<FeatureDefinition> features = testModule.getFeatures();
        assertEquals(3, features.size());

        Iterator<FeatureDefinition> featuresIterator = features.iterator();
        FeatureDefinition feature = featuresIterator.next();
        assertEquals("if-mib", feature.getQName().getLocalName());
        assertEquals("This feature indicates that the device implements IF-MIB.", feature.getDescription());
        assertEquals("RFC 2863: The Interfaces Group MIB", feature.getReference());

        feature = featuresIterator.next();
        assertEquals("pre-provisioning", feature.getQName().getLocalName());
        assertEquals("This feature indicates that the device supports\n" +
                "pre-provisioning of interface configuration, i.e., it is\n" +
                "possible to configure an interface whose physical interface\n" +
                "hardware is not present on the device.", feature.getDescription());

        feature = featuresIterator.next();
        assertEquals("arbitrary-names", feature.getQName().getLocalName());
        assertEquals("This feature indicates that the device allows user-controlled\n" +
                "interfaces to be named arbitrarily.", feature.getDescription());
    }
}
