/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
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
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class DeviationResolutionTest {

    @Test
    public void testDeviateNotSupported() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(
                "/deviation-resolution-test/deviation-not-supported");
        assertNotNull(schemaContext);

        final Module importedModule = schemaContext.findModule("imported", Revision.of("2017-01-20")).get();
        final ContainerSchemaNode myContA = (ContainerSchemaNode) importedModule.getDataChildByName(
                QName.create(importedModule.getQNameModule(), "my-cont-a"));
        assertNotNull(myContA);

        assertEquals(1, myContA.getChildNodes().size());
        assertNotNull(myContA.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-a3")));

        final ContainerSchemaNode myContB = (ContainerSchemaNode) importedModule.getDataChildByName(
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
    public void testDeviateAdd() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(
                sourceForResource("/deviation-resolution-test/deviation-add/foo.yang"),
                sourceForResource("/deviation-resolution-test/deviation-add/bar.yang"));
        assertNotNull(schemaContext);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).get();
        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-leaf-list"));
        assertNotNull(myLeafList);

        assertFalse(myLeafList.isConfiguration());
        assertEquals(3, myLeafList.getDefaults().size());
        assertEquals(10, myLeafList.getConstraints().getMaxElements().intValue());
        assertEquals(5, myLeafList.getConstraints().getMinElements().intValue());
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
        final ContainerSchemaNode input = myRpc.getInput();
        assertEquals(2, input.getConstraints().getMustConstraints().size());
        final ContainerSchemaNode output = myRpc.getOutput();
        assertEquals(2, output.getConstraints().getMustConstraints().size());

        final NotificationDefinition myNotification = barModule.getNotifications().iterator().next();
        assertEquals(2, myNotification.getConstraints().getMustConstraints().size());

        final AnyXmlSchemaNode myAnyxml = (AnyXmlSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-anyxml"));
        assertNotNull(myAnyxml);
        assertTrue(myAnyxml.getConstraints().isMandatory());
        assertEquals(2, myAnyxml.getUnknownSchemaNodes().size());
    }

    @Test
    public void testDeviateReplace() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(
                sourceForResource("/deviation-resolution-test/deviation-replace/foo.yang"),
                sourceForResource("/deviation-resolution-test/deviation-replace/bar.yang"));
        assertNotNull(schemaContext);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).get();
        assertNotNull(barModule);

        final LeafSchemaNode myLeaf = (LeafSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-leaf"));
        assertNotNull(myLeaf);

        assertTrue(myLeaf.getType() instanceof UnsignedIntegerTypeDefinition);
        assertEquals("bytes", myLeaf.getType().getUnits());
        assertEquals("10", myLeaf.getType().getDefaultValue());

        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-leaf-list-test"));
        assertNotNull(myLeafList);

        assertEquals(6, myLeafList.getConstraints().getMaxElements().intValue());
        assertEquals(3, myLeafList.getConstraints().getMinElements().intValue());
        assertTrue(myLeafList.isConfiguration());

        final ChoiceSchemaNode myChoice = (ChoiceSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-choice"));
        assertNotNull(myChoice);

        assertFalse(myChoice.getConstraints().isMandatory());
        assertEquals(1, myChoice.getUnknownSchemaNodes().size());
        assertEquals("new arg", myChoice.getUnknownSchemaNodes().iterator().next().getNodeParameter());

        final ContainerSchemaNode myCont = (ContainerSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-cont"));
        assertNotNull(myCont);

        final LeafSchemaNode myAugLeaf = (LeafSchemaNode) myCont.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-aug-leaf"));
        assertNotNull(myAugLeaf);
        assertTrue(myAugLeaf.getType() instanceof UnsignedIntegerTypeDefinition);
        assertEquals("seconds", myAugLeaf.getType().getUnits());
        assertEquals("new-def-val", myAugLeaf.getType().getDefaultValue());
        assertEquals(1, myAugLeaf.getUnknownSchemaNodes().size());
        assertEquals("new arg", myAugLeaf.getUnknownSchemaNodes().iterator().next().getNodeParameter());

        final LeafSchemaNode myUsedLeaf = (LeafSchemaNode) myCont.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-used-leaf"));
        assertNotNull(myUsedLeaf);
        assertTrue(myUsedLeaf.getType() instanceof UnsignedIntegerTypeDefinition);
        assertEquals("weeks", myUsedLeaf.getType().getUnits());
        assertEquals("new-def-val", myUsedLeaf.getType().getDefaultValue());
        assertEquals(1, myUsedLeaf.getUnknownSchemaNodes().size());
        assertEquals("new arg", myUsedLeaf.getUnknownSchemaNodes().iterator().next().getNodeParameter());
    }

    @Test
    public void testDeviateDelete() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(
                sourceForResource("/deviation-resolution-test/deviation-delete/foo.yang"),
                sourceForResource("/deviation-resolution-test/deviation-delete/bar.yang"));
        assertNotNull(schemaContext);

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-01-20")).get();
        final LeafSchemaNode myLeaf = (LeafSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-leaf"));
        assertNotNull(myLeaf);

        assertNull(myLeaf.getType().getDefaultValue());
        assertNull(myLeaf.getType().getUnits());
        assertEquals(0, myLeaf.getUnknownSchemaNodes().size());

        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-leaf-list"));
        assertNotNull(myLeafList);

        assertEquals(0, myLeafList.getDefaults().size());
        assertEquals(0, myLeafList.getConstraints().getMustConstraints().size());

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
        assertNull(myAugLeaf.getType().getDefaultValue());
        assertNull(myAugLeaf.getType().getUnits());
        assertEquals(0, myAugLeaf.getConstraints().getMustConstraints().size());
        assertEquals(0, myAugLeaf.getUnknownSchemaNodes().size());

        final LeafSchemaNode myUsedLeaf = (LeafSchemaNode) myCont.getDataChildByName(
                QName.create(barModule.getQNameModule(), "my-used-leaf"));
        assertNotNull(myUsedLeaf);
        assertNull(myUsedLeaf.getType().getDefaultValue());
        assertNull(myUsedLeaf.getType().getUnits());
        assertEquals(0, myUsedLeaf.getConstraints().getMustConstraints().size());
        assertEquals(0, myUsedLeaf.getUnknownSchemaNodes().size());
    }

    @Test
    public void shouldFailOnInvalidYang10Model() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-add/foo10-invalid.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-add/bar10-invalid.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."));
        }
    }

    @Test
    public void shouldFailOnInvalidYang10Model2() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-delete/foo10-invalid.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-delete/bar10-invalid.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviationTarget() throws Exception {
        try {
            StmtTestUtils.parseYangSources(sourceForResource(
                    "/deviation-resolution-test/foo-invalid-deviation-target.yang"),
                    sourceForResource("/deviation-resolution-test/bar.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("(bar?revision=2017-01-20)my-cont is not a valid deviation "
                    + "target for substatement (urn:ietf:params:xml:ns:yang:yin:1)max-elements."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviationPath() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/foo-invalid-deviation-path.yang"),
                    sourceForResource("/deviation-resolution-test/bar.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause().getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation target 'Absolute{path=[(bar?revision=2017-01-20)"
                    + "invalid, (bar?revision=2017-01-20)path]}' not found"));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateAdd() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-add/foo-invalid.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-add/bar-invalid.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang"
                    + ":yin:1)config to target node (bar?revision=2017-01-20)my-leaf because it is already defined in"
                    + " target and can appear only once."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateAdd2() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-add/foo-invalid-2.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-add/bar-invalid-2.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang"
                    + ":yin:1)default to target node (bar?revision=2017-01-20)my-leaf because it is already defined in"
                    + " target and can appear only once."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateAdd3() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-add/foo-invalid-4.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-add/bar-invalid-4.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang"
                    + ":yin:1)default to target node (bar?revision=2017-02-01)my-used-leaf because it is already "
                    + "defined in target and can appear only once."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateReplace() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-replace/foo-invalid.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-replace/bar-invalid.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot replace substatement "
                    + "(urn:ietf:params:xml:ns:yang:yin:1)units in target node (bar?revision=2017-01-20)my-leaf "
                    + "because it does not exist in target node."));
        }
    }

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    public void shouldLogInvalidDeviateReplaceAttempt() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, "UTF-8"));

        StmtTestUtils.parseYangSources(
                sourceForResource("/deviation-resolution-test/deviation-replace/foo-invalid-2.yang"),
                sourceForResource("/deviation-resolution-test/deviation-replace/bar-invalid-2.yang"));

        testLog = output.toString();
        System.setOut(stdout);
        assertTrue(testLog.contains("Deviation cannot replace substatement (urn:ietf:params:xml:ns:yang:yin:1)default"
                + " in target leaf-list (bar?revision=2017-01-20)my-leaf-list because a leaf-list can have multiple "
                + "default statements."));
    }

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    public void shouldLogInvalidDeviateDeleteAttempt() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, "UTF-8"));

        StmtTestUtils.parseYangSources(
                sourceForResource("/deviation-resolution-test/deviation-delete/foo-invalid.yang"),
                sourceForResource("/deviation-resolution-test/deviation-delete/bar-invalid.yang"));

        testLog = output.toString();
        System.setOut(stdout);
        assertTrue(testLog.contains("Deviation cannot delete substatement (urn:ietf:params:xml:ns:yang:yin:1)units "
                + "with argument 'seconds' in target node (bar?revision=2017-01-20)my-leaf because it does not exist "
                + "in the target node."));
    }

    @Test
    public void shouldFailOnInvalidDeviateAddSubstatement() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-add/foo-invalid-3.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-add/bar-invalid-3.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("TYPE is not valid for DEVIATE."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateReplaceSubstatement() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-replace/foo-invalid-3.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-replace/bar-invalid-3.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("MUST is not valid for DEVIATE."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateDeleteSubstatement() throws Exception {
        try {
            StmtTestUtils.parseYangSources(
                    sourceForResource("/deviation-resolution-test/deviation-delete/foo-invalid-2.yang"),
                    sourceForResource("/deviation-resolution-test/deviation-delete/bar-invalid-2.yang"));
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("CONFIG is not valid for DEVIATE."));
        }
    }
}
