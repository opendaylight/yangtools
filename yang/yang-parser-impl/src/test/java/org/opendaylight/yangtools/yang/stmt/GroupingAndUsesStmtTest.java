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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class GroupingAndUsesStmtTest {

    private static final StatementStreamSource MODULE = sourceForResource("/model/bar.yang");
    private static final StatementStreamSource SUBMODULE = sourceForResource("/model/subfoo.yang");
    private static final StatementStreamSource GROUPING_MODULE = sourceForResource("/model/baz.yang");
    private static final StatementStreamSource USES_MODULE = sourceForResource("/model/foo.yang");

    @Test
    public void groupingTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(MODULE, GROUPING_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("baz", null);
        assertNotNull(testModule);

        final Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        final Iterator<GroupingDefinition> groupingsIterator = groupings.iterator();
        final GroupingDefinition grouping = groupingsIterator.next();
        assertEquals("target", grouping.getQName().getLocalName());
        assertEquals(5, grouping.getChildNodes().size());

        final AnyXmlSchemaNode anyXmlNode = (AnyXmlSchemaNode) grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "data"));
        assertNotNull(anyXmlNode);
        final ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "how"));
        assertNotNull(choiceNode);
        final LeafSchemaNode leafNode = (LeafSchemaNode) grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "address"));
        assertNotNull(leafNode);
        final ContainerSchemaNode containerNode = (ContainerSchemaNode) grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "port"));
        assertNotNull(containerNode);
        final ListSchemaNode listNode = (ListSchemaNode) grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "addresses"));
        assertNotNull(listNode);

        assertEquals(1, grouping.getGroupings().size());
        assertEquals("target-inner", grouping.getGroupings().iterator().next().getQName().getLocalName());

        assertEquals(1, grouping.getTypeDefinitions().size());
        assertEquals("group-type", grouping.getTypeDefinitions().iterator().next().getQName().getLocalName());

        final List<UnknownSchemaNode> unknownSchemaNodes = grouping.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());
        final UnknownSchemaNode extensionUse = unknownSchemaNodes.get(0);
        assertEquals("opendaylight", extensionUse.getExtensionDefinition().getQName().getLocalName());
    }

    @Test
    public void usesAndRefinesTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(MODULE, SUBMODULE, GROUPING_MODULE, USES_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("foo", null);
        assertNotNull(testModule);

        final Set<UsesNode> usesNodes = testModule.getUses();
        assertEquals(1, usesNodes.size());

        UsesNode usesNode = usesNodes.iterator().next();
        assertEquals("target", usesNode.getGroupingPath().getLastComponent().getLocalName());
        assertEquals(1, usesNode.getAugmentations().size());

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "peer"));
        assertNotNull(container);
        container = (ContainerSchemaNode) container.getDataChildByName(QName.create(testModule.getQNameModule(), "destination"));
        assertEquals(1, container.getUses().size());

        usesNode = container.getUses().iterator().next();
        assertEquals("target", usesNode.getGroupingPath().getLastComponent().getLocalName());

        final Map<SchemaPath, SchemaNode> refines = usesNode.getRefines();
        assertEquals(4, refines.size());

        final Iterator<SchemaPath> refinesKeysIterator = refines.keySet().iterator();
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
