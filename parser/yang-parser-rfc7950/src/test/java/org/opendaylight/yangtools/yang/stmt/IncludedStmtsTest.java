/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class IncludedStmtsTest extends AbstractYangTest {
    private static EffectiveModelContext result;

    @BeforeAll
    static void setup() {
        result = assertEffectiveModelDir("/included-statements-test");
    }

    @AfterAll
    static void teardown() {
        result = null;
    }

    @Test
    void includedTypedefsTest() {
        final var testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        assertThat(testModule.getTypeDefinitions())
            .hasSize(2)
            .allSatisfy(typedef -> {
                assertThat(typedef.getQName().getLocalName()).matches(str -> switch (str) {
                    case "new-int32-type", "new-string-type" -> true;
                    default -> false;
                });
                final var baseType = typedef.getBaseType();
                assertNotNull(baseType);
                assertThat(baseType.getQName().getLocalName()).matches(str -> switch (str) {
                    case "int32", "string" -> true;
                    default -> false;
                });
            });
    }

    @Test
    void includedFeaturesTest() {
        final var testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        assertThat(testModule.getFeatures())
            .hasSize(2)
            .allSatisfy(feature -> {
                assertThat(feature.getQName().getLocalName()).matches(str -> switch (str) {
                    case "new-feature1", "new-feature2" -> true;
                    default -> false;
                });
            });
    }

    @Test
    void includedContainersAndListsTest() {
        final var testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        var cont = assertInstanceOf(ContainerSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "parent-container")));
        cont = assertInstanceOf(ContainerSchemaNode.class,
            cont.getDataChildByName(QName.create(testModule.getQNameModule(), "child-container")));
        assertEquals(2, cont.getChildNodes().size());

        assertInstanceOf(LeafSchemaNode.class,
            cont.getDataChildByName(QName.create(testModule.getQNameModule(), "autumn-leaf")));
        assertInstanceOf(LeafSchemaNode.class,
            cont.getDataChildByName(QName.create(testModule.getQNameModule(), "winter-snow")));
    }

    @Test
    void submoduleNamespaceTest() {
        final var testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);
        final var subModule = testModule.getSubmodules().iterator().next();
        assertEquals("urn:opendaylight.org/root-module", subModule.getNamespace().toString());
    }
}
