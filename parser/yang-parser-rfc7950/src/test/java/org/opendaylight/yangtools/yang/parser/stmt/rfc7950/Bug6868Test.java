/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

class Bug6868Test extends AbstractYangTest {
    private static final String FOO_NS = "foo";
    private static final String IMP_NS = "imp";
    private static final String IMP_REV = "2017-01-09";
    private static final Set<String> ALL_CONTAINERS = ImmutableSet.of("my-container-1", "my-container-2",
        "my-container-3", "foo", "not-foo", "imp-bar", "imp-bar-2");

    @Test
    void ifFeatureYang11ResolutionTest() throws Exception {
        assertSchemaContextFor(null, ALL_CONTAINERS);
        assertSchemaContextFor(ImmutableSet.of(), ImmutableSet.of("my-container-1", "my-container-2", "not-foo"));
        assertSchemaContextFor(ImmutableSet.of("foo"), ImmutableSet.of("foo"));
        assertSchemaContextFor(ImmutableSet.of("baz"),
            ImmutableSet.of("my-container-1", "my-container-2", "my-container-3", "not-foo"));
        assertSchemaContextFor(ImmutableSet.of("bar", "baz"),
            ImmutableSet.of("my-container-1", "my-container-2", "my-container-3", "not-foo"));
        assertSchemaContextFor(ImmutableSet.of("foo", "bar", "baz"),
            ImmutableSet.of("my-container-1", "my-container-2", "my-container-3", "foo"));
        assertSchemaContextFor(ImmutableSet.of("foo", "bar", "baz", "imp:bar"),
            ImmutableSet.of("my-container-1", "my-container-2", "my-container-3", "foo", "imp-bar"));
        assertSchemaContextFor(ImmutableSet.of("foo", "baz", "imp:bar"),
            ImmutableSet.of("foo", "imp-bar", "imp-bar-2"));
    }

    private static void assertSchemaContextFor(final Set<String> supportedFeatures,
        final Set<String> expectedContainers) throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/rfc7950/bug6868/yang11",
            supportedFeatures != null ? createFeaturesSet(supportedFeatures) : null, YangParserConfiguration.DEFAULT);
        assertNotNull(schemaContext);

        for (final String expectedContainer : expectedContainers) {
            assertThat(String.format("Expected container %s not found.", expectedContainer),
                schemaContext.findDataTreeChild(QName.create(FOO_NS, expectedContainer)).get(),
                instanceOf(ContainerSchemaNode.class));
        }

        final Set<String> unexpectedContainers = Sets.difference(ALL_CONTAINERS, expectedContainers);
        for (final String unexpectedContainer : unexpectedContainers) {
            assertEquals(Optional.empty(),
                schemaContext.findDataTreeChild(QName.create(FOO_NS, unexpectedContainer)),
                String.format("Unexpected container %s.", unexpectedContainer));
        }
    }

    private static Set<QName> createFeaturesSet(final Set<String> featureNames) {
        final Set<QName> supportedFeatures = new HashSet<>();
        for (final String featureName : featureNames) {
            if (featureName.indexOf(':') == -1) {
                supportedFeatures.add(QName.create(FOO_NS, featureName));
            } else {
                supportedFeatures
                    .add(QName.create(IMP_NS, IMP_REV, featureName.substring(featureName.indexOf(':') + 1)));
            }
        }

        return ImmutableSet.copyOf(supportedFeatures);
    }

    @Test
    void invalidYang10Test() {
        assertSourceException(startsWith("Invalid identifier '(not foo) or (bar and baz)' [at "),
            "/rfc7950/bug6868/invalid10.yang");
    }
}