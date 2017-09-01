/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileFeatureStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws ReactorException, SAXException, IOException, URISyntaxException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/feature-test/")
            .toURI());
        assertEquals(1, context.getModules().size());
    }

    @Test
    public void testFeature() {
        Module testModule = TestUtils.findModule(context, "yang-with-features").get();
        assertNotNull(testModule);

        Set<FeatureDefinition> features = testModule.getFeatures();
        assertEquals(2, features.size());

        Iterator<FeatureDefinition> featuresIterator = features.iterator();
        FeatureDefinition feature = featuresIterator.next();

        assertThat(feature.getQName().getLocalName(), anyOf(is("arbitrary-names"), is("pre-provisioning")));

        feature = featuresIterator.next();

        assertThat(feature.getQName().getLocalName(), anyOf(is("arbitrary-names"), is("pre-provisioning")));
    }
}
