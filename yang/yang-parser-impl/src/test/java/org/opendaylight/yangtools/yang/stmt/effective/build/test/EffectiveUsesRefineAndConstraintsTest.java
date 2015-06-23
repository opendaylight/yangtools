package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.stmt.test.StmtTestUtils;
import org.junit.Test;

public class EffectiveUsesRefineAndConstraintsTest {

    private static final YangStatementSourceImpl REFINE_TEST = new YangStatementSourceImpl(
            "/stmt-test/uses/refine-test.yang",false);

    @Test
    public void refineTest() throws SourceException, ReactorException,
            URISyntaxException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        StmtTestUtils.addSources(reactor, REFINE_TEST);

        EffectiveSchemaContext result = reactor.buildEffective();

        assertNotNull(result);

        Set<Module> modules = result.getModules();
        assertNotNull(modules);
        assertEquals(1, modules.size());

        Module module = modules.iterator().next();

        QNameModule qnameModule = module.getQNameModule();
        QName rootContainer = QName.create(qnameModule, "root-container");
        QName grp1 = QName.create(qnameModule, "grp-1");

        QName containerFromGrouping = QName.create(qnameModule,
                "container-from-grouping");
        QName listInContainer = QName.create(qnameModule, "list-in-container");
        QName choiceFromGrp = QName.create(qnameModule, "choice-from-grp");

        QName containerFromGrouping2 = QName.create(qnameModule,
                "container-from-grouping2");
        QName presenceContainer = QName.create(qnameModule,
                "presence-container");

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

    private void checkOriginalContainer(EffectiveSchemaContext result,
            SchemaPath path) {
        SchemaNode containerInContainerNode = SchemaContextUtil
                .findDataSchemaNode(result, path);
        assertNotNull(containerInContainerNode);

        ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) containerInContainerNode;
        assertNull(containerSchemaNode.getReference());
        assertNull(containerSchemaNode.getDescription());
        assertEquals(true, containerSchemaNode.isConfiguration());
        assertEquals(false, containerSchemaNode.isPresenceContainer());

        ConstraintDefinition containerConstraints = containerSchemaNode
                .getConstraints();
        assertEquals(0, containerConstraints.getMustConstraints().size());
    }

    private void checkOriginalChoice(EffectiveSchemaContext result,
            SchemaPath path) {
        SchemaNode choiceInContainerNode = SchemaContextUtil
                .findDataSchemaNode(result, path);
        assertNotNull(choiceInContainerNode);

        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) choiceInContainerNode;

        ConstraintDefinition choiceConstraints = choiceSchemaNode
                .getConstraints();
        assertTrue(choiceConstraints.isMandatory() == false);
        assertTrue(choiceConstraints.getMustConstraints().isEmpty());
    }

    private void checkOriginalList(EffectiveSchemaContext result,
            SchemaPath path) {
        SchemaNode listInContainerNode = SchemaContextUtil.findDataSchemaNode(
                result, path);
        assertNotNull(listInContainerNode);

        ListSchemaNode listSchemaNode = (ListSchemaNode) listInContainerNode;
        assertEquals("original reference", listSchemaNode.getReference());
        assertEquals("original description", listSchemaNode.getDescription());
        assertEquals(false, listSchemaNode.isConfiguration());

        ConstraintDefinition listConstraints = listSchemaNode.getConstraints();
        assertTrue(listConstraints.getMinElements() == 10);
        assertTrue(listConstraints.getMaxElements() == 20);
        assertEquals(1, listConstraints.getMustConstraints().size());
    }

    private void checkRefinedContainer(EffectiveSchemaContext result,
            SchemaPath path) {
        SchemaNode containerInContainerNode = SchemaContextUtil
                .findDataSchemaNode(result, path);
        assertNotNull(containerInContainerNode);

        ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) containerInContainerNode;
        assertEquals("new reference", containerSchemaNode.getReference());
        assertEquals("new description", containerSchemaNode.getDescription());
        assertEquals(true, containerSchemaNode.isConfiguration());
        assertEquals(true, containerSchemaNode.isPresenceContainer());

        ConstraintDefinition containerConstraints = containerSchemaNode
                .getConstraints();
        assertEquals(1, containerConstraints.getMustConstraints().size());
    }

    private void checkRefinedChoice(EffectiveSchemaContext result,
            SchemaPath path) {
        SchemaNode choiceInContainerNode = SchemaContextUtil
                .findDataSchemaNode(result, path);
        assertNotNull(choiceInContainerNode);

        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) choiceInContainerNode;

        ConstraintDefinition choiceConstraints = choiceSchemaNode
                .getConstraints();
        assertTrue(choiceConstraints.isMandatory());
        assertTrue(choiceConstraints.getMustConstraints().isEmpty());
    }

    private void checkRefinedList(EffectiveSchemaContext result, SchemaPath path) {
        SchemaNode listInContainerNode = SchemaContextUtil.findDataSchemaNode(
                result, path);
        assertNotNull(listInContainerNode);

        ListSchemaNode listSchemaNode = (ListSchemaNode) listInContainerNode;
        assertEquals("new reference", listSchemaNode.getReference());
        assertEquals("new description", listSchemaNode.getDescription());
        assertEquals(true, listSchemaNode.isConfiguration());

        ConstraintDefinition listConstraints = listSchemaNode.getConstraints();
        assertTrue(listConstraints.getMinElements() == 5);
        assertTrue(listConstraints.getMaxElements() == 7);
        assertEquals(2, listConstraints.getMustConstraints().size());
    }
}
