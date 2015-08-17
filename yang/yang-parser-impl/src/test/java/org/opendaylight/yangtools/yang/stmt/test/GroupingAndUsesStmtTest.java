/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class GroupingAndUsesStmtTest {

    private static final YangStatementSourceImpl MODULE = new YangStatementSourceImpl("/model/bar.yang", false);
    private static final YangStatementSourceImpl SUBMODULE = new YangStatementSourceImpl("/model/subfoo.yang", false);
    private static final YangStatementSourceImpl GROUPING_MODULE = new YangStatementSourceImpl("/model/baz.yang",
            false);
    private static final YangStatementSourceImpl USES_MODULE = new YangStatementSourceImpl("/model/foo.yang", false);

    @Test
    public void groupingTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, MODULE, GROUPING_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("baz", null);
        assertNotNull(testModule);

        Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        Iterator<GroupingDefinition> groupingsIterator = groupings.iterator();
        GroupingDefinition grouping = groupingsIterator.next();
        assertEquals("target", grouping.getQName().getLocalName());
        assertEquals(5, grouping.getChildNodes().size());

        AnyXmlSchemaNode anyXmlNode = (AnyXmlSchemaNode) grouping.getDataChildByName("data");
        assertNotNull(anyXmlNode);
        ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) grouping.getDataChildByName("how");
        assertNotNull(choiceNode);
        LeafSchemaNode leafNode = (LeafSchemaNode) grouping.getDataChildByName("address");
        assertNotNull(leafNode);
        ContainerSchemaNode containerNode = (ContainerSchemaNode) grouping.getDataChildByName("port");
        assertNotNull(containerNode);
        ListSchemaNode listNode = (ListSchemaNode) grouping.getDataChildByName("addresses");
        assertNotNull(listNode);

        assertEquals(1, grouping.getGroupings().size());
        assertEquals("target-inner", grouping.getGroupings().iterator().next().getQName().getLocalName());

        assertEquals(1, grouping.getTypeDefinitions().size());
        assertEquals("group-type", grouping.getTypeDefinitions().iterator().next().getQName().getLocalName());

        List<UnknownSchemaNode> unknownSchemaNodes = grouping.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());
        UnknownSchemaNode extensionUse = unknownSchemaNodes.get(0);
        assertEquals("opendaylight", extensionUse.getExtensionDefinition().getQName().getLocalName());
    }

    @Test
    public void usesAndRefinesTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, MODULE, SUBMODULE, GROUPING_MODULE, USES_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("foo", null);
        assertNotNull(testModule);

        Set<UsesNode> usesNodes = testModule.getUses();
        assertEquals(1, usesNodes.size());

        UsesNode usesNode = usesNodes.iterator().next();
        assertEquals("target", usesNode.getGroupingPath().getLastComponent().getLocalName());
        assertEquals(1, usesNode.getAugmentations().size());

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName("peer");
        assertNotNull(container);
        container = (ContainerSchemaNode) container.getDataChildByName("destination");
        assertEquals(1, container.getUses().size());

        usesNode = container.getUses().iterator().next();
        assertEquals("target", usesNode.getGroupingPath().getLastComponent().getLocalName());

        Map<SchemaPath, SchemaNode> refines = usesNode.getRefines();
        assertEquals(4, refines.size());

        Iterator<SchemaPath> refinesKeysIterator = refines.keySet().iterator();
        SchemaPath path = refinesKeysIterator.next();
        assertThat(path.getLastComponent().getLocalName(), anyOf(is("port"), is("address"), is("addresses"), is("id")));
        path = refinesKeysIterator.next();
        assertThat(path.getLastComponent().getLocalName(), anyOf(is("port"), is("address"), is("addresses"), is("id")));
        path = refinesKeysIterator.next();
        assertThat(path.getLastComponent().getLocalName(), anyOf(is("port"), is("address"), is("addresses"), is("id")));
        path = refinesKeysIterator.next();
        assertThat(path.getLastComponent().getLocalName(), anyOf(is("port"), is("address"), is("addresses"), is("id")));
    }
}
