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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
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
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class DeviationResolutionTest {

    @Test
    public void testDeviateNotSupported() throws ReactorException, FileNotFoundException, URISyntaxException,
            ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(
                "/deviation-resolution-test/deviation-not-supported");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-01-20");

        final Module rootModule = schemaContext.findModuleByName("root", revision);
        assertNotNull(rootModule);

        final ContainerSchemaNode myContA = (ContainerSchemaNode) rootModule.getDataChildByName(
                QName.create(rootModule.getQNameModule(), "my-cont-a"));
        assertNotNull(myContA);

        assertEquals(1, myContA.getChildNodes().size());
        assertNotNull(myContA.getDataChildByName(QName.create(rootModule.getQNameModule(), "my-leaf-a3")));

        final ContainerSchemaNode myContB = (ContainerSchemaNode) rootModule.getDataChildByName(
                QName.create(rootModule.getQNameModule(), "my-cont-b"));
        assertNull(myContB);

        final Module importedModule = schemaContext.findModuleByName("imported", revision);
        assertNotNull(importedModule);

        final ContainerSchemaNode myContC = (ContainerSchemaNode) importedModule.getDataChildByName(
                QName.create(importedModule.getQNameModule(), "my-cont-c"));
        assertNotNull(myContC);

        assertEquals(2, myContC.getChildNodes().size());
        assertNotNull(myContC.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-c1")));
        assertNotNull(myContC.getDataChildByName(QName.create(importedModule.getQNameModule(), "my-leaf-c2")));
    }

    @Test
    public void testDeviateAdd() throws ReactorException, FileNotFoundException, URISyntaxException, ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource(
                "/deviation-resolution-test/deviation-add/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-01-20");

        final Module fooModule = schemaContext.findModuleByName("foo", revision);
        assertNotNull(fooModule);

        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-leaf-list"));
        assertNotNull(myLeafList);

        assertFalse(myLeafList.isConfiguration());
        assertEquals(3, myLeafList.getDefaults().size());
        assertEquals(10, myLeafList.getConstraints().getMaxElements().intValue());
        assertEquals(5, myLeafList.getConstraints().getMinElements().intValue());
        assertNotNull(myLeafList.getType().getUnits());

        final ListSchemaNode myList = (ListSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-list"));
        assertNotNull(myList);
        assertEquals(2, myList.getUniqueConstraints().size());

        final ChoiceSchemaNode myChoice = (ChoiceSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-choice"));
        assertNotNull(myChoice);
        assertEquals("c2", myChoice.getDefaultCase());

        final RpcDefinition myRpc = fooModule.getRpcs().iterator().next();
        final ContainerSchemaNode input = myRpc.getInput();
        assertEquals(2, input.getConstraints().getMustConstraints().size());
        final ContainerSchemaNode output = myRpc.getOutput();
        assertEquals(2, output.getConstraints().getMustConstraints().size());

        final NotificationDefinition myNotification = fooModule.getNotifications().iterator().next();
        assertEquals(2, myNotification.getConstraints().getMustConstraints().size());

        final AnyXmlSchemaNode myAnyxml = (AnyXmlSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-anyxml"));
        assertNotNull(myAnyxml);
        assertTrue(myAnyxml.getConstraints().isMandatory());
        assertEquals(2, myAnyxml.getUnknownSchemaNodes().size());
    }

    @Test
    public void testDeviateReplace() throws ReactorException, FileNotFoundException, URISyntaxException, ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource(
                "/deviation-resolution-test/deviation-replace/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-01-20");

        final Module fooModule = schemaContext.findModuleByName("foo", revision);
        assertNotNull(fooModule);

        final LeafSchemaNode myLeaf = (LeafSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-leaf"));
        assertNotNull(myLeaf);

        assertTrue(myLeaf.getType() instanceof UnsignedIntegerTypeDefinition);
        assertNotNull(myLeaf.getUnits());
        assertEquals("bytes", myLeaf.getUnits());
        assertNotNull(myLeaf.getDefault());
        assertEquals("10", myLeaf.getDefault());

        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-leaf-list-test"));
        assertNotNull(myLeafList);

        assertEquals(6, myLeafList.getConstraints().getMaxElements().intValue());
        assertEquals(3, myLeafList.getConstraints().getMinElements().intValue());
        assertTrue(myLeafList.isConfiguration());

        final ChoiceSchemaNode myChoice = (ChoiceSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-choice"));
        assertNotNull(myChoice);

        assertFalse(myChoice.getConstraints().isMandatory());
        assertEquals(1, myChoice.getUnknownSchemaNodes().size());
        assertEquals("new arg", myChoice.getUnknownSchemaNodes().iterator().next().getNodeParameter());

        final ContainerSchemaNode myCont = (ContainerSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-cont"));
        assertNotNull(myCont);

        final LeafSchemaNode myAugLeaf = (LeafSchemaNode) myCont.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-aug-leaf"));
        assertNotNull(myAugLeaf);
        assertTrue(myAugLeaf.getType() instanceof UnsignedIntegerTypeDefinition);
        assertNotNull(myAugLeaf.getUnits());
        assertEquals("seconds", myAugLeaf.getUnits());
        assertNotNull(myAugLeaf.getDefault());
        assertEquals("new-def-val", myAugLeaf.getDefault());
        assertEquals(1, myAugLeaf.getUnknownSchemaNodes().size());
        assertEquals("new arg", myAugLeaf.getUnknownSchemaNodes().iterator().next().getNodeParameter());

        final LeafSchemaNode myUsedLeaf = (LeafSchemaNode) myCont.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-used-leaf"));
        assertNotNull(myUsedLeaf);
        assertTrue(myUsedLeaf.getType() instanceof UnsignedIntegerTypeDefinition);
        assertNotNull(myUsedLeaf.getUnits());
        assertEquals("weeks", myUsedLeaf.getUnits());
        assertNotNull(myUsedLeaf.getDefault());
        assertEquals("new-def-val", myUsedLeaf.getDefault());
        assertEquals(1, myUsedLeaf.getUnknownSchemaNodes().size());
        assertEquals("new arg", myUsedLeaf.getUnknownSchemaNodes().iterator().next().getNodeParameter());
    }

    @Test
    public void testDeviateDelete() throws ReactorException, FileNotFoundException, URISyntaxException, ParseException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource(
                "/deviation-resolution-test/deviation-delete/foo.yang");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-01-20");

        final Module fooModule = schemaContext.findModuleByName("foo", revision);
        assertNotNull(fooModule);

        final LeafSchemaNode myLeaf = (LeafSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-leaf"));
        assertNotNull(myLeaf);

        assertNull(myLeaf.getDefault());
        assertNull(myLeaf.getUnits());
        assertEquals(0, myLeaf.getUnknownSchemaNodes().size());

        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-leaf-list"));
        assertNotNull(myLeafList);

        assertEquals(0, myLeafList.getDefaults().size());
        assertEquals(0, myLeafList.getConstraints().getMustConstraints().size());

        final ListSchemaNode myList = (ListSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-list"));
        assertNotNull(myList);

        assertEquals(0, myList.getUniqueConstraints().size());
        assertEquals(0, myList.getUnknownSchemaNodes().size());

        final ContainerSchemaNode myCont = (ContainerSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-cont"));
        assertNotNull(myCont);

        final LeafSchemaNode myAugLeaf = (LeafSchemaNode) myCont.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-aug-leaf"));
        assertNotNull(myAugLeaf);
        assertNull(myAugLeaf.getDefault());
        assertNull(myAugLeaf.getUnits());
        assertEquals(0, myAugLeaf.getConstraints().getMustConstraints().size());
        assertEquals(0, myAugLeaf.getUnknownSchemaNodes().size());

        final LeafSchemaNode myUsedLeaf = (LeafSchemaNode) myCont.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "my-used-leaf"));
        assertNotNull(myUsedLeaf);
        assertNull(myUsedLeaf.getDefault());
        assertNull(myUsedLeaf.getUnits());
        assertEquals(0, myUsedLeaf.getConstraints().getMustConstraints().size());
        assertEquals(0, myUsedLeaf.getUnknownSchemaNodes().size());
    }

    @Test
    public void shouldFailOnInvalidYang10Model() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-add/foo10-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."));
        }
    }

    @Test
    public void shouldFailOnInvalidYang10Model2() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-delete/foo10-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("Maximal count of DEFAULT for DEVIATE is 1, detected 2."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviationTarget() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/foo-invalid-deviation-target.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("(foo?revision=2017-01-20)my-cont is not a valid deviation " +
                    "target for substatement (urn:ietf:params:xml:ns:yang:yin:1)max-elements."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviationPath() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/foo-invalid-deviation-path.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause().getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation target 'Absolute{path=[(foo?revision=2017-01-20)invalid, " +
                    "(foo?revision=2017-01-20)path]}' not found"));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateAdd() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-add/foo-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang" +
                    ":yin:1)config to target node (foo?revision=2017-01-20)my-leaf because it is already defined in" +
                    " target and can appear only once."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateAdd2() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-add/foo-invalid-2.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang" +
                    ":yin:1)default to target node (foo?revision=2017-01-20)my-leaf because it is already defined in" +
                    " target and can appear only once."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateAdd3() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-add/foo-invalid-4.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot add substatement (urn:ietf:params:xml:ns:yang" +
                    ":yin:1)default to target node (foo?revision=2017-02-01)my-used-leaf because it is already " +
                    "defined in target and can appear only once."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateReplace() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-replace/foo-invalid.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InferenceException);
            assertTrue(cause.getMessage().startsWith("Deviation cannot replace substatement " +
                    "(urn:ietf:params:xml:ns:yang:yin:1)units in target node (foo?revision=2017-01-20)my-leaf " +
                    "because it does not exist in target node."));
        }
    }

    @Test
    public void shouldLogInvalidDeviateReplaceAttempt() throws FileNotFoundException, URISyntaxException,
            UnsupportedEncodingException, ReactorException {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, "UTF-8"));

        StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-replace/foo-invalid-2.yang");

        testLog = output.toString();
        assertTrue(testLog.contains("Deviation cannot replace substatement (urn:ietf:params:xml:ns:yang:yin:1)default" +
                " in target leaf-list (foo?revision=2017-01-20)my-leaf-list because a leaf-list can have multiple " +
                "default statements."));
        System.setOut(stdout);
    }

    @Test
    public void shouldLogInvalidDeviateDeleteAttempt() throws FileNotFoundException, URISyntaxException,
            UnsupportedEncodingException, ReactorException {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, "UTF-8"));

        StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-delete/foo-invalid.yang");

        testLog = output.toString();
        assertTrue(testLog.contains("Deviation cannot delete substatement (urn:ietf:params:xml:ns:yang:yin:1)units " +
                "with argument 'seconds' in target node (foo?revision=2017-01-20)my-leaf because it does not exist " +
                "in the target node."));
        System.setOut(stdout);
    }

    @Test
    public void shouldFailOnInvalidDeviateAddSubstatement() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-add/foo-invalid-3.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("TYPE is not valid for DEVIATE."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateReplaceSubstatement() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-replace/foo-invalid-3.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("MUST is not valid for DEVIATE."));
        }
    }

    @Test
    public void shouldFailOnInvalidDeviateDeleteSubstatement() throws FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/deviation-resolution-test/deviation-delete/foo-invalid-2.yang");
            fail("An exception should have been thrown.");
        } catch (final ReactorException ex) {
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof InvalidSubstatementException);
            assertTrue(cause.getMessage().startsWith("CONFIG is not valid for DEVIATE."));
        }
    }
}
