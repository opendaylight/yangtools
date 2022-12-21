/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Collection;
import java.util.Iterator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

class IncludedStmtsTest {
    private static SchemaContext result;

    @BeforeAll
    static void setup() throws ReactorException {
        result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/included-statements-test/root-module.yang"))
            .addSource(sourceForResource("/included-statements-test/child-module.yang"))
            .buildEffective();
    }

    @AfterAll
    static void teardown() {
        result = null;
    }

    @Test
    void includedTypedefsTest() {
        final Module testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        final Collection<? extends TypeDefinition<?>> typedefs = testModule.getTypeDefinitions();
        assertEquals(2, typedefs.size());

        final Iterator<? extends TypeDefinition<?>> typedefsIterator = typedefs.iterator();
        TypeDefinition<?> typedef = typedefsIterator.next();
        assertThat(typedef.getQName().getLocalName(), anyOf(is("new-string-type"), is("new-int32-type")));
        assertThat(typedef.getBaseType().getQName().getLocalName(), anyOf(is("string"), is("int32")));
        typedef = typedefsIterator.next();
        assertThat(typedef.getQName().getLocalName(), anyOf(is("new-string-type"), is("new-int32-type")));
        assertThat(typedef.getBaseType().getQName().getLocalName(), anyOf(is("string"), is("int32")));
    }

    @Test
    void includedFeaturesTest() {
        final Module testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        final Collection<? extends FeatureDefinition> features = testModule.getFeatures();
        assertEquals(2, features.size());

        final Iterator<? extends FeatureDefinition> featuresIterator = features.iterator();
        FeatureDefinition feature = featuresIterator.next();
        assertThat(feature.getQName().getLocalName(), anyOf(is("new-feature1"), is("new-feature2")));
        feature = featuresIterator.next();
        assertThat(feature.getQName().getLocalName(), anyOf(is("new-feature1"), is("new-feature2")));
    }

    @Test
    void includedContainersAndListsTest() {
        final Module testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        ContainerSchemaNode cont = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "parent-container"));
        assertNotNull(cont);
        cont = (ContainerSchemaNode) cont.getDataChildByName(
            QName.create(testModule.getQNameModule(), "child-container"));
        assertNotNull(cont);
        assertEquals(2, cont.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) cont.getDataChildByName(
            QName.create(testModule.getQNameModule(), "autumn-leaf"));
        assertNotNull(leaf);
        leaf = (LeafSchemaNode) cont.getDataChildByName(QName.create(testModule.getQNameModule(), "winter-snow"));
        assertNotNull(leaf);
    }

    @Test
    void submoduleNamespaceTest() {
        final Module testModule = result.findModules("root-module").iterator().next();
        assertNotNull(testModule);

        final Submodule subModule = testModule.getSubmodules().iterator().next();
        assertEquals("urn:opendaylight.org/root-module", subModule.getNamespace().toString());
    }
}
