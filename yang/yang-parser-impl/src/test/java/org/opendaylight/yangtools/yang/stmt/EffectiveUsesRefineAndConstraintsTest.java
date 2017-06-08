/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class EffectiveUsesRefineAndConstraintsTest {

    private static final StatementStreamSource REFINE_TEST = sourceForResource("/stmt-test/uses/refine-test.yang");

    @Test
    public void refineTest() throws SourceException, ReactorException,
            URISyntaxException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();

        reactor.addSources(REFINE_TEST);

        SchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Set<Module> modules = result.getModules();
        assertNotNull(modules);
        assertEquals(1, modules.size());

        Module module = modules.iterator().next();

        QNameModule qnameModule = module.getQNameModule();
        QName rootContainer = QName.create(qnameModule, "root-container");
        QName grp1 = QName.create(qnameModule, "grp-1");

        QName containerFromGrouping = QName.create(qnameModule, "container-from-grouping");
        QName listInContainer = QName.create(qnameModule, "list-in-container");
        QName choiceFromGrp = QName.create(qnameModule, "choice-from-grp");

        QName containerFromGrouping2 = QName.create(qnameModule, "container-from-grouping2");
        QName presenceContainer = QName.create(qnameModule, "presence-container");

        SchemaPath listInContainerPath = SchemaPath.create(true, rootContainer,
                containerFromGrouping, listInContainer);
        SchemaPath choiceFromGrpPath = SchemaPath.create(true, rootContainer,
                containerFromGrouping, choiceFromGrp);
        SchemaPath presenceContainerPath = SchemaPath.create(true,
                rootContainer, containerFromGrouping2, presenceContainer);

        checkRefinedList(result, listInContainerPath);
        checkRefinedChoice(result, choiceFromGrpPath);
        checkRefinedContainer(result, presenceContainerPath);

        SchemaPath originalListInContainerPath = SchemaPath.create(true, grp1,
                containerFromGrouping, listInContainer);
        SchemaPath originalChoiceFromGrpPath = SchemaPath.create(true, grp1,
                containerFromGrouping, choiceFromGrp);
        SchemaPath originalPresenceContainerPath = SchemaPath.create(true,
                grp1, containerFromGrouping2, presenceContainer);

        checkOriginalList(result, originalListInContainerPath);
        checkOriginalChoice(result, originalChoiceFromGrpPath);
        checkOriginalContainer(result, originalPresenceContainerPath);
    }

    private static void checkOriginalContainer(final SchemaContext result, final SchemaPath path) {
        SchemaNode containerInContainerNode = SchemaContextUtil.findDataSchemaNode(result, path);
        assertNotNull(containerInContainerNode);

        ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) containerInContainerNode;
        assertNull(containerSchemaNode.getReference());
        assertNull(containerSchemaNode.getDescription());
        assertTrue(containerSchemaNode.isConfiguration());
        assertFalse(containerSchemaNode.isPresenceContainer());

        ConstraintDefinition containerConstraints = containerSchemaNode.getConstraints();
        assertEquals(0, containerConstraints.getMustConstraints().size());
    }

    private static void checkOriginalChoice(final SchemaContext result, final SchemaPath path) {
        SchemaNode choiceInContainerNode = SchemaContextUtil.findDataSchemaNode(result, path);
        assertNotNull(choiceInContainerNode);

        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) choiceInContainerNode;

        ConstraintDefinition choiceConstraints = choiceSchemaNode
                .getConstraints();
        assertFalse(choiceConstraints.isMandatory());
        assertTrue(choiceConstraints.getMustConstraints().isEmpty());
    }

    private static void checkOriginalList(final SchemaContext result, final SchemaPath path) {
        SchemaNode listInContainerNode = SchemaContextUtil.findDataSchemaNode(result, path);
        assertNotNull(listInContainerNode);

        ListSchemaNode listSchemaNode = (ListSchemaNode) listInContainerNode;
        assertEquals("original reference", listSchemaNode.getReference());
        assertEquals("original description", listSchemaNode.getDescription());
        assertFalse(listSchemaNode.isConfiguration());

        ConstraintDefinition listConstraints = listSchemaNode.getConstraints();
        assertEquals(10, listConstraints.getMinElements().intValue());
        assertEquals(20, listConstraints.getMaxElements().intValue());
        assertEquals(1, listConstraints.getMustConstraints().size());
    }

    private static void checkRefinedContainer(final SchemaContext result, final SchemaPath path) {
        SchemaNode containerInContainerNode = SchemaContextUtil.findDataSchemaNode(result, path);
        assertNotNull(containerInContainerNode);

        ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) containerInContainerNode;
        assertEquals("new reference", containerSchemaNode.getReference());
        assertEquals("new description", containerSchemaNode.getDescription());
        assertTrue(containerSchemaNode.isConfiguration());
        assertTrue(containerSchemaNode.isPresenceContainer());

        ConstraintDefinition containerConstraints = containerSchemaNode.getConstraints();
        assertEquals(1, containerConstraints.getMustConstraints().size());
    }

    private static void checkRefinedChoice(final SchemaContext result, final SchemaPath path) {
        SchemaNode choiceInContainerNode = SchemaContextUtil.findDataSchemaNode(result, path);
        assertNotNull(choiceInContainerNode);

        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) choiceInContainerNode;

        ConstraintDefinition choiceConstraints = choiceSchemaNode
                .getConstraints();
        assertTrue(choiceConstraints.isMandatory());
        assertTrue(choiceConstraints.getMustConstraints().isEmpty());
    }

    private static void checkRefinedList(final SchemaContext result, final SchemaPath path) {
        SchemaNode listInContainerNode = SchemaContextUtil.findDataSchemaNode(result, path);
        assertNotNull(listInContainerNode);

        ListSchemaNode listSchemaNode = (ListSchemaNode) listInContainerNode;
        assertEquals("new reference", listSchemaNode.getReference());
        assertEquals("new description", listSchemaNode.getDescription());
        assertTrue(listSchemaNode.isConfiguration());

        ConstraintDefinition listConstraints = listSchemaNode.getConstraints();
        assertEquals(5, listConstraints.getMinElements().intValue());
        assertEquals(7, listConstraints.getMaxElements().intValue());
        assertEquals(2, listConstraints.getMustConstraints().size());
    }
}
