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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class IncludedStmtsTest {

    private static final YangStatementSourceImpl ROOT_MODULE = new YangStatementSourceImpl
            ("/included-statements-test/root-module.yang", false);
    private static final YangStatementSourceImpl CHILD_MODULE = new YangStatementSourceImpl
            ("/included-statements-test/child-module.yang", false);

    @Test
    public void includedTypedefsTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, CHILD_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("root-module", null);
        assertNotNull(testModule);

        Set<TypeDefinition<?>> typedefs = testModule.getTypeDefinitions();
        assertEquals(2, typedefs.size());

        Iterator<TypeDefinition<?>> typedefsIterator = typedefs.iterator();
        TypeDefinition<?> typedef = typedefsIterator.next();
        assertThat(typedef.getQName().getLocalName(), anyOf(is("new-string-type"), is("new-int32-type")));
        assertThat(typedef.getBaseType().getQName().getLocalName(), anyOf(is("string"), is("int32")));
        typedef = typedefsIterator.next();
        assertThat(typedef.getQName().getLocalName(), anyOf(is("new-string-type"), is("new-int32-type")));
        assertThat(typedef.getBaseType().getQName().getLocalName(), anyOf(is("string"), is("int32")));
    }

    @Test
    public void includedFeaturesTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, CHILD_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("root-module", null);
        assertNotNull(testModule);

        Set<FeatureDefinition> features = testModule.getFeatures();
        assertEquals(2, features.size());

        Iterator<FeatureDefinition> featuresIterator = features.iterator();
        FeatureDefinition feature = featuresIterator.next();
        assertThat(feature.getQName().getLocalName(), anyOf(is("new-feature1"), is("new-feature2")));
        feature = featuresIterator.next();
        assertThat(feature.getQName().getLocalName(), anyOf(is("new-feature1"), is("new-feature2")));
    }

    @Test
    public void includedContainersAndListsTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, CHILD_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("root-module", null);
        assertNotNull(testModule);

        ContainerSchemaNode cont = (ContainerSchemaNode) testModule.getDataChildByName("parent-container");
        assertNotNull(cont);
        cont = (ContainerSchemaNode) cont.getDataChildByName("child-container");
        assertNotNull(cont);
        assertEquals(2, cont.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) cont.getDataChildByName("autumn-leaf");
        assertNotNull(leaf);
        leaf = (LeafSchemaNode) cont.getDataChildByName("winter-snow");
        assertNotNull(leaf);
    }

    @Test
    public void submoduleNamespaceTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, ROOT_MODULE, CHILD_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("root-module", null);
        assertNotNull(testModule);

        Module subModule = testModule.getSubmodules().iterator().next();
        assertEquals("urn:opendaylight.org/root-module", subModule.getNamespace().toString());
    }
}
