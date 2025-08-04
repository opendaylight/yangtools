/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;

class DeviationResolutionTest extends AbstractYangTest {
    @Test
    void testDeviateNotSupported() {
        final var schemaContext = assertEffectiveModelDir("/deviation-resolution-test/deviation-not-supported");

        final var importedModule = schemaContext.findModule("imported", Revision.of("2017-01-20")).orElseThrow();
        final var myContA = assertInstanceOf(ContainerSchemaNode.class,
            importedModule.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-cont-a")));

        assertEquals(1, myContA.getChildNodes().size());
        assertNotNull(myContA.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-a3")));

        assertNull(importedModule.dataChildByName(QName.create(importedModule.getQNameModule(), "my-cont-b")));

        final var myContC = assertInstanceOf(ContainerSchemaNode.class,
            importedModule.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-cont-c")));

        assertEquals(2, myContC.getChildNodes().size());
        assertNotNull(myContC.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-c1")));
        assertNotNull(myContC.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-c2")));
    }

    @Test
    void testDeviateAdd() {
        final var schemaContext = assertEffectiveModel(
            "/deviation-resolution-test/deviation-add/foo.yang",
            "/deviation-resolution-test/deviation-add/bar.yang");

        final var barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).orElseThrow();
        final var myLeafList = assertInstanceOf(LeafListSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-leaf-list")));

        assertEquals(Optional.of(Boolean.FALSE), myLeafList.effectiveConfig());
        assertEquals(3, myLeafList.getDefaults().size());

        final var constraint = myLeafList.getElementCountConstraint().orElseThrow();
        assertEquals((Object) 10, constraint.getMaxElements());
        assertEquals((Object) 5, constraint.getMinElements());
        assertNotNull(myLeafList.getType().getUnits());

        final var myList = assertInstanceOf(ListSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-list")));
        assertEquals(2, myList.getUniqueConstraints().size());

        final var myChoice = assertInstanceOf(ChoiceSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-choice")));
        assertEquals("c2", myChoice.getDefaultCase().orElseThrow().getQName().getLocalName());

        final var myRpc = barModule.getRpcs().iterator().next();
        final var input = myRpc.getInput();
        assertEquals(2, input.getMustConstraints().size());
        final var output = myRpc.getOutput();
        assertEquals(2, output.getMustConstraints().size());

        final var myNotification = barModule.getNotifications().iterator().next();
        assertEquals(2, myNotification.getMustConstraints().size());

        final var myAnyxml = assertInstanceOf(AnyxmlSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-anyxml")));
        assertTrue(myAnyxml.isMandatory());

        final var myAnyData = assertInstanceOf(AnydataSchemaNode.class,
            barModule.findDataChildByName(QName.create(barModule.getQNameModule(), "my-anydata")).orElseThrow());
        assertTrue(myAnyData.isMandatory());
    }

    @Test
    void testDeviateReplace() {
        final var schemaContext = assertEffectiveModel(
            "/deviation-resolution-test/deviation-replace/foo.yang",
            "/deviation-resolution-test/deviation-replace/bar.yang");

        final var barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).orElseThrow();
        final var myLeaf = assertInstanceOf(LeafSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-leaf")));

        assertInstanceOf(Uint32TypeDefinition.class, myLeaf.getType());
        assertEquals(Optional.of("bytes"), myLeaf.getType().getUnits());
        assertEquals(Optional.of("10"), myLeaf.getType().getDefaultValue());

        final var myLeafList = assertInstanceOf(LeafListSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-leaf-list-test")));
        final var constraint = myLeafList.getElementCountConstraint().orElseThrow();
        assertEquals((Object) 6, constraint.getMaxElements());
        assertEquals((Object) 3, constraint.getMinElements());
        assertEquals(Optional.of(Boolean.TRUE), myLeafList.effectiveConfig());

        final var myChoice = assertInstanceOf(ChoiceSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-choice")));
        assertFalse(myChoice.isMandatory());
        // FIXME: we need a supported extension to properly test this
        assertEquals(0, myChoice.getUnknownSchemaNodes().size());

        final var myCont = assertInstanceOf(ContainerSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-cont")));

        final var myAugLeaf = assertInstanceOf(LeafSchemaNode.class,
            myCont.getDataChildByName(QName.create(barModule.getQNameModule(), "my-aug-leaf")));
        assertNotNull(myAugLeaf);
        assertInstanceOf(Uint32TypeDefinition.class, myAugLeaf.getType());
        assertEquals(Optional.of("seconds"), myAugLeaf.getType().getUnits());
        assertEquals(Optional.of("new-def-val"), myAugLeaf.getType().getDefaultValue());
        // FIXME: we need a supported extension to properly test this
        assertEquals(0, myAugLeaf.getUnknownSchemaNodes().size());

        final var myUsedLeaf = assertInstanceOf(LeafSchemaNode.class,
            myCont.getDataChildByName(QName.create(barModule.getQNameModule(), "my-used-leaf")));
        assertInstanceOf(Uint32TypeDefinition.class, myUsedLeaf.getType());
        assertEquals(Optional.of("weeks"), myUsedLeaf.getType().getUnits());
        assertEquals(Optional.of("new-def-val"), myUsedLeaf.getType().getDefaultValue());
        // FIXME: we need a supported extension to properly test this
        assertEquals(0, myUsedLeaf.getUnknownSchemaNodes().size());
    }

    @Test
    void testDeviateDelete() {
        final var schemaContext = assertEffectiveModel(
            "/deviation-resolution-test/deviation-delete/foo.yang",
            "/deviation-resolution-test/deviation-delete/bar.yang");

        final var barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).orElseThrow();
        final var myLeaf = assertInstanceOf(LeafSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-leaf")));

        assertEquals(Optional.empty(), myLeaf.getType().getDefaultValue());
        assertEquals(Optional.empty(), myLeaf.getType().getUnits());
        assertEquals(0, myLeaf.getUnknownSchemaNodes().size());

        final var myLeafList = assertInstanceOf(LeafListSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-leaf-list")));
        assertEquals(0, myLeafList.getDefaults().size());
        assertEquals(0, myLeafList.getMustConstraints().size());

        final var myList = assertInstanceOf(ListSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-list")));
        assertEquals(0, myList.getUniqueConstraints().size());
        assertEquals(0, myList.getUnknownSchemaNodes().size());

        final var myCont = assertInstanceOf(ContainerSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "my-cont")));
        final var myAugLeaf = assertInstanceOf(LeafSchemaNode.class,
            myCont.getDataChildByName(QName.create(barModule.getQNameModule(), "my-aug-leaf")));
        assertEquals(Optional.empty(), myAugLeaf.getType().getDefaultValue());
        assertEquals(Optional.empty(), myAugLeaf.getType().getUnits());
        assertEquals(0, myAugLeaf.getMustConstraints().size());
        assertEquals(0, myAugLeaf.getUnknownSchemaNodes().size());

        final var myUsedLeaf = assertInstanceOf(LeafSchemaNode.class,
            myCont.getDataChildByName(QName.create(barModule.getQNameModule(), "my-used-leaf")));
        assertNotNull(myUsedLeaf);
        assertEquals(Optional.empty(), myUsedLeaf.getType().getDefaultValue());
        assertEquals(Optional.empty(), myUsedLeaf.getType().getUnits());
        assertEquals(0, myUsedLeaf.getMustConstraints().size());
        assertEquals(0, myUsedLeaf.getUnknownSchemaNodes().size());
    }

    @Test
    void shouldFailOnInvalidYang10Model() {
        assertInvalidSubstatementException(startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."),
            "/deviation-resolution-test/deviation-add/foo10-invalid.yang",
            "/deviation-resolution-test/deviation-add/bar10-invalid.yang");
    }

    @Test
    void shouldFailOnInvalidYang10Model2() {
        assertInvalidSubstatementException(startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."),
            "/deviation-resolution-test/deviation-delete/foo10-invalid.yang",
            "/deviation-resolution-test/deviation-delete/bar10-invalid.yang");
    }

    @Test
    void shouldFailOnInvalidDeviationTarget() {
        assertInvalidStateException(startsWith("(bar?revision=2017-01-20)my-cont is not a valid deviation "
            + "target for substatement (urn:ietf:params:xml:ns:yang:yin:1)max-elements."),
            "/deviation-resolution-test/foo-invalid-deviation-target.yang",
            "/deviation-resolution-test/bar.yang");
    }

    @Test
    void shouldFailOnInvalidDeviationPath() {
        assertInvalidStateException(startsWith(
            "Deviation target 'Absolute{qnames=[(bar?revision=2017-01-20)invalid, path]}' not found"),
            "/deviation-resolution-test/foo-invalid-deviation-path.yang",
            "/deviation-resolution-test/bar.yang");
    }

    @Test
    void shouldFailOnInvalidDeviateAdd() {
        assertInvalidStateException(startsWith("""
            Deviation cannot add substatement (urn:ietf:params:xml:ns:yang:yin:1)config to target node \
            (bar?revision=2017-01-20)my-leaf because it is already defined in target and can appear only once."""),
            "/deviation-resolution-test/deviation-add/foo-invalid.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid.yang");
    }

    @Test
    void shouldFailOnInvalidDeviateAdd2() {
        assertInvalidStateException(startsWith("""
            Deviation cannot add substatement (urn:ietf:params:xml:ns:yang:yin:1)default to target node \
            (bar?revision=2017-01-20)my-leaf because it is already defined in target and can appear only once."""),
            "/deviation-resolution-test/deviation-add/foo-invalid-2.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid-2.yang");
    }

    @Test
    void shouldFailOnInvalidDeviateAdd3() {
        assertInvalidStateException(startsWith("""
            Deviation cannot add substatement (urn:ietf:params:xml:ns:yang:yin:1)default to target node \
            (bar?revision=2017-02-01)my-used-leaf because it is already defined in target and can appear only once."""),
            "/deviation-resolution-test/deviation-add/foo-invalid-4.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid-4.yang");
    }

    @Test
    void shouldFailOnInvalidDeviateReplace() {
        assertInvalidStateException(startsWith("""
            Deviation cannot replace substatement (urn:ietf:params:xml:ns:yang:yin:1)units in target node \
            (bar?revision=2017-01-20)my-leaf because it does not exist in target node."""),
            "/deviation-resolution-test/deviation-replace/foo-invalid.yang",
            "/deviation-resolution-test/deviation-replace/bar-invalid.yang");
    }

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    void shouldLogInvalidDeviateReplaceAttempt() throws Exception {
        final var stdout = System.out;
        final var output = new ByteArrayOutputStream();

        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-replace/foo-invalid-2.yang",
            "/deviation-resolution-test/deviation-replace/bar-invalid-2.yang");

        final var testLog = output.toString();
        System.setOut(stdout);
        assertThat(testLog).contains("""
            Deviation cannot replace substatement (urn:ietf:params:xml:ns:yang:yin:1)default in target leaf-list \
            (bar?revision=2017-01-20)my-leaf-list because a leaf-list can have multiple default statements.""");
    }

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    void shouldLogInvalidDeviateDeleteAttempt() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-delete/foo-invalid.yang",
            "/deviation-resolution-test/deviation-delete/bar-invalid.yang");

        testLog = output.toString();
        System.setOut(stdout);
        assertThat(testLog).contains(
            "Deviation cannot delete substatement (urn:ietf:params:xml:ns:yang:yin:1)units with argument 'seconds' in "
                + "target node (bar?revision=2017-01-20)my-leaf because it does not exist in the target node.");
    }

    @Test
    void shouldFailOnInvalidDeviateAddSubstatement() {
        assertInvalidSubstatementException(startsWith("TYPE is not valid for DEVIATE."),
            "/deviation-resolution-test/deviation-add/foo-invalid-3.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid-3.yang");
    }

    @Test
    void shouldFailOnInvalidDeviateReplaceSubstatement() {
        assertInvalidSubstatementException(startsWith("MUST is not valid for DEVIATE."),
            "/deviation-resolution-test/deviation-replace/foo-invalid-3.yang",
            "/deviation-resolution-test/deviation-replace/bar-invalid-3.yang");
    }

    @Test
    void shouldFailOnInvalidDeviateDeleteSubstatement() {
        assertInvalidSubstatementException(startsWith("CONFIG is not valid for DEVIATE."),
            "/deviation-resolution-test/deviation-delete/foo-invalid-2.yang",
            "/deviation-resolution-test/deviation-delete/bar-invalid-2.yang");
    }
}
