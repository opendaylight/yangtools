/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class AugmentProcessTest extends AbstractYangTest {
    private static final StatementStreamSource AUGMENTED = sourceForResource("/stmt-test/augments/augmented.yang");
    private static final StatementStreamSource ROOT = sourceForResource("/stmt-test/augments/aug-root.yang");

    private static final QNameModule ROOT_QNAME_MODULE = QNameModule.of("root");
    private static final QNameModule AUGMENTED_QNAME_MODULE = QNameModule.of("aug");

    private final QName augParent1 = QName.create(AUGMENTED_QNAME_MODULE, "aug-parent1");
    private final QName augParent2 = QName.create(AUGMENTED_QNAME_MODULE, "aug-parent2");
    private final QName contTarget = QName.create(AUGMENTED_QNAME_MODULE, "cont-target");

    private final QName contAdded1 = QName.create(ROOT_QNAME_MODULE, "cont-added1");
    private final QName contAdded2 = QName.create(ROOT_QNAME_MODULE, "cont-added2");

    private final QName list1 = QName.create(ROOT_QNAME_MODULE, "list1");
    private final QName axml = QName.create(ROOT_QNAME_MODULE, "axml");

    private final QName contGrp = QName.create(ROOT_QNAME_MODULE, "cont-grp");
    private final QName axmlGrp = QName.create(ROOT_QNAME_MODULE, "axml-grp");

    private final QName augCont1 = QName.create(ROOT_QNAME_MODULE, "aug-cont1");
    private final QName augCont2 = QName.create(ROOT_QNAME_MODULE, "aug-cont2");

    private final QName grpCont2 = QName.create(ROOT_QNAME_MODULE, "grp-cont2");
    private final QName grpCont22 = QName.create(ROOT_QNAME_MODULE, "grp-cont22");
    private final QName grpAdd = QName.create(ROOT_QNAME_MODULE, "grp-add");

    private static final StatementStreamSource MULTIPLE_AUGMENT = sourceForResource(
        "/stmt-test/augments/multiple-augment-test.yang");

    private static final StatementStreamSource MULTIPLE_AUGMENT_ROOT = sourceForResource(
        "/stmt-test/augments/multiple-augment-root.yang");
    private static final StatementStreamSource MULTIPLE_AUGMENT_IMPORTED = sourceForResource(
        "/stmt-test/augments/multiple-augment-imported.yang");
    private static final StatementStreamSource MULTIPLE_AUGMENT_SUBMODULE = sourceForResource(
        "/stmt-test/augments/multiple-augment-submodule.yang");

    private static final StatementStreamSource MULTIPLE_AUGMENT_INCORRECT = sourceForResource(
        "/stmt-test/augments/multiple-augment-incorrect.yang");

    private static final StatementStreamSource MULTIPLE_AUGMENT_INCORRECT2 = sourceForResource(
        "/stmt-test/augments/multiple-augment-incorrect2.yang");

    @Test
    void multipleAugmentsAndMultipleModulesTest() throws ReactorException {
        final var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(MULTIPLE_AUGMENT_ROOT, MULTIPLE_AUGMENT_IMPORTED, MULTIPLE_AUGMENT_SUBMODULE)
            .buildEffective();
        assertNotNull(result);
    }

    @Test
    void multipleAugmentTest() throws ReactorException {
        final var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(MULTIPLE_AUGMENT)
            .buildEffective();
        assertNotNull(result);
    }

    @Test
    void multipleAugmentIncorrectPathTest() {
        assertThrows(SomeModifiersUnresolvedException.class, () -> RFC7950Reactors.defaultReactor().newBuild()
            .addSource(MULTIPLE_AUGMENT_INCORRECT)
            .buildEffective());
    }

    @Test
    void multipleAugmentIncorrectPathAndGrpTest() {
        assertThrows(SomeModifiersUnresolvedException.class, () -> RFC7950Reactors.defaultReactor().newBuild()
            .addSource(MULTIPLE_AUGMENT_INCORRECT2)
            .buildEffective());
    }

    @Test
    void readAndParseYangFileTest() throws ReactorException {
        final var root = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(AUGMENTED, ROOT)
            .buildEffective();
        assertNotNull(root);

        final var augmentedModule = root.findModules("augmented").iterator().next();
        assertNotNull(augmentedModule);

        final var augParent1Node = (ContainerSchemaNode) root.getDataChildByName(augParent1);
        final var augParent2Node = (ContainerSchemaNode) augParent1Node.getDataChildByName(augParent2);
        final var targetContNode = (ContainerSchemaNode) augParent2Node.getDataChildByName(contTarget);
        assertEquals(3, targetContNode.getChildNodes().size());

        final var contAdded1Node = (ContainerSchemaNode) targetContNode.getDataChildByName(contAdded1);
        assertInstanceOf(ListSchemaNode.class, contAdded1Node.dataChildByName(list1));

        final var contAdded2Node = (ContainerSchemaNode) targetContNode.getDataChildByName(contAdded2);
        assertInstanceOf(AnyxmlSchemaNode.class, contAdded2Node.dataChildByName(axml));

        final var contGrpNode = (ContainerSchemaNode) targetContNode.getDataChildByName(contGrp);
        assertInstanceOf(AnyxmlSchemaNode.class, contGrpNode.dataChildByName(axmlGrp));

        final var augCont1Node = (ContainerSchemaNode) root.getDataChildByName(augCont1);
        final var augCont2Node = (ContainerSchemaNode) augCont1Node.getDataChildByName(augCont2);
        final var grpCont2Node = (ContainerSchemaNode) augCont2Node.getDataChildByName(grpCont2);
        final var grpCont22Node = (ContainerSchemaNode) grpCont2Node.getDataChildByName(grpCont22);
        assertInstanceOf(ContainerSchemaNode.class, grpCont22Node.dataChildByName(grpAdd));
    }

    @Test
    void caseShortHandAugmentingTest() {
        final var context = assertEffectiveModelDir("/choice-case-type-test-models");

        assertNotNull(context);

        final String rev = "2013-07-01";
        final String ns = "urn:ietf:params:xml:ns:yang:choice-monitoring";
        final String nsAug = "urn:ietf:params:xml:ns:yang:augment-monitoring";

        final var netconf = assertInstanceOf(ContainerSchemaNode.class,
            context.dataChildByName(QName.create(ns, rev, "netconf-state")));
        final var datastores = assertInstanceOf(ContainerSchemaNode.class,
            netconf.dataChildByName(QName.create(ns, rev, "datastores")));
        final var datastore = assertInstanceOf(ListSchemaNode.class,
            datastores.dataChildByName(QName.create(ns, rev, "datastore")));
        final var locks = assertInstanceOf(ContainerSchemaNode.class,
            datastore.getDataChildByName(QName.create(ns, rev, "locks")));
        final var lockType = assertInstanceOf(ChoiceSchemaNode.class,
            locks.getDataChildByName(QName.create(ns, rev, "lock-type")));

        final var leafAugCase = lockType.findCaseNodes("leaf-aug-case").iterator().next();
        assertTrue(leafAugCase.isAugmenting());
        final var leafAug = leafAugCase.getDataChildByName(QName.create(nsAug, rev, "leaf-aug-case"));
        assertFalse(leafAug.isAugmenting());
    }
}
