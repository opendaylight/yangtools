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

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

class Bug6868Test extends AbstractYangTest {
    private static final String FOO_NS = "foo";
    private static final String IMP_NS = "imp";
    private static final String IMP_REV = "2017-01-09";
    private static final Set<String> ALL_CONTAINERS = Set.of("my-container-1", "my-container-2",
        "my-container-3", "foo", "not-foo", "imp-bar", "imp-bar-2");

    @Test
    void ifFeatureYang11ResolutionTest() throws Exception {
        assertSchemaContextFor(null, ALL_CONTAINERS);
        assertSchemaContextFor(Set.of(), Set.of("my-container-1", "my-container-2", "not-foo"));
        assertSchemaContextFor(Set.of("foo"), Set.of("foo"));
        assertSchemaContextFor(Set.of("baz"), Set.of("my-container-1", "my-container-2", "my-container-3", "not-foo"));
        assertSchemaContextFor(Set.of("bar", "baz"),
            Set.of("my-container-1", "my-container-2", "my-container-3", "not-foo"));
        assertSchemaContextFor(Set.of("foo", "bar", "baz"),
            Set.of("my-container-1", "my-container-2", "my-container-3", "foo"));
        assertSchemaContextFor(Set.of("foo", "bar", "baz", "imp:bar"),
            Set.of("my-container-1", "my-container-2", "my-container-3", "foo", "imp-bar"));
        assertSchemaContextFor(Set.of("foo", "baz", "imp:bar"),
            Set.of("foo", "imp-bar", "imp-bar-2"));
    }

    private static void assertSchemaContextFor(final Set<String> supportedFeatures,
            final Set<String> expectedContainers) throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSources("/rfc7950/bug6868/yang11",
            supportedFeatures != null ? createFeaturesSet(supportedFeatures) : null, YangParserConfiguration.DEFAULT);
        assertNotNull(schemaContext);

        for (var expectedContainer : expectedContainers) {
            assertThat(String.format("Expected container %s not found.", expectedContainer),
                schemaContext.findDataTreeChild(QName.create(FOO_NS, expectedContainer)).get(),
                instanceOf(ContainerSchemaNode.class));
        }

        for (var unexpectedContainer : Sets.difference(ALL_CONTAINERS, expectedContainers)) {
            assertEquals(Optional.empty(),
                schemaContext.findDataTreeChild(QName.create(FOO_NS, unexpectedContainer)),
                String.format("Unexpected container %s.", unexpectedContainer));
        }
    }

    private static Set<QName> createFeaturesSet(final Set<String> featureNames) {
        final Set<QName> supportedFeatures = new HashSet<>();
        for (var featureName : featureNames) {
            if (featureName.indexOf(':') == -1) {
                supportedFeatures.add(QName.create(FOO_NS, featureName));
            } else {
                supportedFeatures
                    .add(QName.create(IMP_NS, IMP_REV, featureName.substring(featureName.indexOf(':') + 1)));
            }
        }

        return Set.copyOf(supportedFeatures);
    }

    @Test
    void invalidYang10Test() {
        assertSourceException(startsWith("Invalid identifier '(not foo) or (bar and baz)' [at "),
            "/rfc7950/bug6868/invalid10.yang");
    }
}
