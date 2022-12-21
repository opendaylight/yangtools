/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

class YinFileFeatureStmtTest {
    private EffectiveModelContext context;

    @BeforeEach
    void init() throws Exception {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/feature-test/")
            .toURI());
        assertEquals(1, context.getModules().size());
    }

    @Test
    void testFeature() {
        Module testModule = context.findModules("yang-with-features").iterator().next();
        assertNotNull(testModule);

        Collection<? extends FeatureDefinition> features = testModule.getFeatures();
        assertEquals(2, features.size());

        Iterator<? extends FeatureDefinition> featuresIterator = features.iterator();
        FeatureDefinition feature = featuresIterator.next();

        assertThat(feature.getQName().getLocalName(), anyOf(is("arbitrary-names"), is("pre-provisioning")));

        feature = featuresIterator.next();

        assertThat(feature.getQName().getLocalName(), anyOf(is("arbitrary-names"), is("pre-provisioning")));
    }
}
