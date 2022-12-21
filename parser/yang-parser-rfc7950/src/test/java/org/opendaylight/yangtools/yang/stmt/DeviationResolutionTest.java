/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

class DeviationResolutionTest extends AbstractYangTest {
    @Test
    void testDeviateNotSupported() throws Exception {
        final var schemaContext = assertEffectiveModelDir("/deviation-resolution-test/deviation-not-supported");
        assertNotNull(schemaContext);

        final Module importedModule = schemaContext.findModule("imported", Revision.of("2017-01-20")).get();
        final ContainerSchemaNode myContA = (ContainerSchemaNode) importedModule.getDataChildByName(
            QName.create(importedModule.getQNameModule(), "my-cont-a"));
        assertNotNull(myContA);

        assertEquals(1, myContA.getChildNodes().size());
        assertNotNull(myContA.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-a3")));

        final ContainerSchemaNode myContB = (ContainerSchemaNode) importedModule.dataChildByName(
            QName.create(importedModule.getQNameModule(), "my-cont-b"));
        assertNull(myContB);

        final ContainerSchemaNode myContC = (ContainerSchemaNode) importedModule.getDataChildByName(
            QName.create(importedModule.getQNameModule(), "my-cont-c"));
        assertNotNull(myContC);

        assertEquals(2, myContC.getChildNodes().size());
        assertNotNull(myContC.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-c1")));
        assertNotNull(myContC.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-c2")));
    }

    @Test
    void testDeviateAdd() throws Exception {
        final EffectiveModelContext schemaContext = TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-add/foo.yang",
            "/deviation-resolution-test/deviation-add/bar.yang");
        assertNotNull(schemaContext);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).get();
        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-leaf-list"));
        assertNotNull(myLeafList);

        assertEquals(Optional.of(Boolean.FALSE), myLeafList.effectiveConfig());
        assertEquals(3, myLeafList.getDefaults().size());

        final ElementCountConstraint constraint = myLeafList.getElementCountConstraint().get();
        assertEquals((Object) 10, constraint.getMaxElements());
        assertEquals((Object) 5, constraint.getMinElements());
        assertNotNull(myLeafList.getType().getUnits());

        final ListSchemaNode myList = (ListSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-list"));
        assertNotNull(myList);
        assertEquals(2, myList.getUniqueConstraints().size());

        final ChoiceSchemaNode myChoice = (ChoiceSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-choice"));
        assertNotNull(myChoice);
        assertEquals("c2", myChoice.getDefaultCase().get().getQName().getLocalName());

        final RpcDefinition myRpc = barModule.getRpcs().iterator().next();
        final InputSchemaNode input = myRpc.getInput();
        assertEquals(2, input.getMustConstraints().size());
        final OutputSchemaNode output = myRpc.getOutput();
        assertEquals(2, output.getMustConstraints().size());

        final NotificationDefinition myNotification = barModule.getNotifications().iterator().next();
        assertEquals(2, myNotification.getMustConstraints().size());

        final AnyxmlSchemaNode myAnyxml = (AnyxmlSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-anyxml"));
        assertNotNull(myAnyxml);
        assertTrue(myAnyxml.isMandatory());

        final AnydataSchemaNode myAnyData = (AnydataSchemaNode) barModule.findDataChildByName(
            QName.create(barModule.getQNameModule(), "my-anydata")).orElse(null);
        assertNotNull(myAnyData);
        assertTrue(myAnyData.isMandatory());
    }

    @Test
    void testDeviateReplace() throws Exception {
        final EffectiveModelContext schemaContext = TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-replace/foo.yang",
            "/deviation-resolution-test/deviation-replace/bar.yang");
        assertNotNull(schemaContext);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).get();
        assertNotNull(barModule);

        final LeafSchemaNode myLeaf = (LeafSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-leaf"));
        assertNotNull(myLeaf);

        assertInstanceOf(Uint32TypeDefinition.class, myLeaf.getType());
        assertEquals(Optional.of("bytes"), myLeaf.getType().getUnits());
        assertEquals(Optional.of("10"), myLeaf.getType().getDefaultValue());

        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-leaf-list-test"));
        assertNotNull(myLeafList);

        final ElementCountConstraint constraint = myLeafList.getElementCountConstraint().get();
        assertEquals((Object) 6, constraint.getMaxElements());
        assertEquals((Object) 3, constraint.getMinElements());
        assertEquals(Optional.of(Boolean.TRUE), myLeafList.effectiveConfig());

        final ChoiceSchemaNode myChoice = (ChoiceSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-choice"));
        assertNotNull(myChoice);

        assertFalse(myChoice.isMandatory());
        // FIXME: we need a supported extension to properly test this
        assertEquals(0, myChoice.getUnknownSchemaNodes().size());

        final ContainerSchemaNode myCont = (ContainerSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-cont"));
        assertNotNull(myCont);

        final LeafSchemaNode myAugLeaf = (LeafSchemaNode) myCont.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-aug-leaf"));
        assertNotNull(myAugLeaf);
        assertInstanceOf(Uint32TypeDefinition.class, myAugLeaf.getType());
        assertEquals(Optional.of("seconds"), myAugLeaf.getType().getUnits());
        assertEquals(Optional.of("new-def-val"), myAugLeaf.getType().getDefaultValue());
        // FIXME: we need a supported extension to properly test this
        assertEquals(0, myAugLeaf.getUnknownSchemaNodes().size());

        final LeafSchemaNode myUsedLeaf = (LeafSchemaNode) myCont.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-used-leaf"));
        assertNotNull(myUsedLeaf);
        assertInstanceOf(Uint32TypeDefinition.class, myUsedLeaf.getType());
        assertEquals(Optional.of("weeks"), myUsedLeaf.getType().getUnits());
        assertEquals(Optional.of("new-def-val"), myUsedLeaf.getType().getDefaultValue());
        // FIXME: we need a supported extension to properly test this
        assertEquals(0, myUsedLeaf.getUnknownSchemaNodes().size());
    }

    @Test
    void testDeviateDelete() throws Exception {
        final EffectiveModelContext schemaContext = TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-delete/foo.yang",
            "/deviation-resolution-test/deviation-delete/bar.yang");
        assertNotNull(schemaContext);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).get();
        final LeafSchemaNode myLeaf = (LeafSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-leaf"));
        assertNotNull(myLeaf);

        assertEquals(Optional.empty(), myLeaf.getType().getDefaultValue());
        assertEquals(Optional.empty(), myLeaf.getType().getUnits());
        assertEquals(0, myLeaf.getUnknownSchemaNodes().size());

        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-leaf-list"));
        assertNotNull(myLeafList);

        assertEquals(0, myLeafList.getDefaults().size());
        assertEquals(0, myLeafList.getMustConstraints().size());

        final ListSchemaNode myList = (ListSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-list"));
        assertNotNull(myList);

        assertEquals(0, myList.getUniqueConstraints().size());
        assertEquals(0, myList.getUnknownSchemaNodes().size());

        final ContainerSchemaNode myCont = (ContainerSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-cont"));
        assertNotNull(myCont);

        final LeafSchemaNode myAugLeaf = (LeafSchemaNode) myCont.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-aug-leaf"));
        assertNotNull(myAugLeaf);
        assertEquals(Optional.empty(), myAugLeaf.getType().getDefaultValue());
        assertEquals(Optional.empty(), myAugLeaf.getType().getUnits());
        assertEquals(0, myAugLeaf.getMustConstraints().size());
        assertEquals(0, myAugLeaf.getUnknownSchemaNodes().size());

        final LeafSchemaNode myUsedLeaf = (LeafSchemaNode) myCont.getDataChildByName(
            QName.create(barModule.getQNameModule(), "my-used-leaf"));
        assertNotNull(myUsedLeaf);
        assertEquals(Optional.empty(), myUsedLeaf.getType().getDefaultValue());
        assertEquals(Optional.empty(), myUsedLeaf.getType().getUnits());
        assertEquals(0, myUsedLeaf.getMustConstraints().size());
        assertEquals(0, myUsedLeaf.getUnknownSchemaNodes().size());
    }

    @Test
    void shouldFailOnInvalidYang10Model() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-add/foo10-invalid.yang",
            "/deviation-resolution-test/deviation-add/bar10-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InvalidSubstatementException.class, cause);
        assertThat(cause.getMessage(), startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."));
    }

    @Test
    void shouldFailOnInvalidYang10Model2() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-delete/foo10-invalid.yang",
            "/deviation-resolution-test/deviation-delete/bar10-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InvalidSubstatementException.class, cause);
        assertThat(cause.getMessage(), startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."));
    }

    @Test
    void shouldFailOnInvalidDeviationTarget() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/foo-invalid-deviation-target.yang",
            "/deviation-resolution-test/bar.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage(), startsWith("(bar?revision=2017-01-20)my-cont is not a valid deviation "
            + "target for substatement (urn:ietf:params:xml:ns:yang:yin:1)max-elements."));
    }

    @Test
    void shouldFailOnInvalidDeviationPath() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/foo-invalid-deviation-path.yang",
            "/deviation-resolution-test/bar.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage(), startsWith(
            "Deviation target 'Absolute{qnames=[(bar?revision=2017-01-20)invalid, path]}' not found"));
    }

    @Test
    void shouldFailOnInvalidDeviateAdd() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-add/foo-invalid.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang"
            + ":yin:1)config to target node (bar?revision=2017-01-20)my-leaf because it is already defined in"
            + " target and can appear only once."));
    }

    @Test
    void shouldFailOnInvalidDeviateAdd2() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-add/foo-invalid-2.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid-2.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang"
            + ":yin:1)default to target node (bar?revision=2017-01-20)my-leaf because it is already defined in"
            + " target and can appear only once."));
    }

    @Test
    void shouldFailOnInvalidDeviateAdd3() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-add/foo-invalid-4.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid-4.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang"
            + ":yin:1)default to target node (bar?revision=2017-02-01)my-used-leaf because it is already "
            + "defined in target and can appear only once."));
    }

    @Test
    void shouldFailOnInvalidDeviateReplace() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-replace/foo-invalid.yang",
            "/deviation-resolution-test/deviation-replace/bar-invalid.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InferenceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Deviation cannot replace substatement "
            + "(urn:ietf:params:xml:ns:yang:yin:1)units in target node (bar?revision=2017-01-20)my-leaf "
            + "because it does not exist in target node."));
    }

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    void shouldLogInvalidDeviateReplaceAttempt() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-replace/foo-invalid-2.yang",
            "/deviation-resolution-test/deviation-replace/bar-invalid-2.yang");

        testLog = output.toString();
        System.setOut(stdout);
        assertThat(testLog, containsString(
            "Deviation cannot replace substatement (urn:ietf:params:xml:ns:yang:yin:1)default in target leaf-list "
                + "(bar?revision=2017-01-20)my-leaf-list because a leaf-list can have multiple "
                + "default statements."));
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
        assertThat(testLog, containsString(
            "Deviation cannot delete substatement (urn:ietf:params:xml:ns:yang:yin:1)units with argument 'seconds' in "
                + "target node (bar?revision=2017-01-20)my-leaf because it does not exist in the target node."));
    }

    @Test
    void shouldFailOnInvalidDeviateAddSubstatement() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-add/foo-invalid-3.yang",
            "/deviation-resolution-test/deviation-add/bar-invalid-3.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InvalidSubstatementException.class, cause);
        assertThat(cause.getMessage(), startsWith("TYPE is not valid for DEVIATE."));
    }

    @Test
    void shouldFailOnInvalidDeviateReplaceSubstatement() {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-replace/foo-invalid-3.yang",
            "/deviation-resolution-test/deviation-replace/bar-invalid-3.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InvalidSubstatementException.class, cause);
        assertThat(cause.getMessage(), startsWith("MUST is not valid for DEVIATE."));
    }

    @Test
    void shouldFailOnInvalidDeviateDeleteSubstatement() throws Exception {
        final ReactorException ex = assertThrows(ReactorException.class, () -> TestUtils.parseYangSource(
            "/deviation-resolution-test/deviation-delete/foo-invalid-2.yang",
            "/deviation-resolution-test/deviation-delete/bar-invalid-2.yang"));
        final Throwable cause = ex.getCause();
        assertInstanceOf(InvalidSubstatementException.class, cause);
        assertThat(cause.getMessage(), startsWith("CONFIG is not valid for DEVIATE."));
    }
}
