/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class CaseStmtTest {
    private SchemaContext schema;
    private Module rootFoo;
    private Module rootBar;
    private QNameModule qnameFoo;
    private QNameModule qnameBar;
    private DataSchemaNode tempChild;
    private DataSchemaNode tempSecondChild;
    private DataSchemaNode tempThirdChild;
    private ChoiceCaseNode tempChoice;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        schema = StmtTestUtils.parseYangSources("/case-test");
        Revision rev = Revision.of("2015-09-09");
        rootFoo = schema.findModule("foo", rev).get();
        rootBar = schema.findModule("bar", rev).get();
        assertNotNull(rootFoo);
        assertNotNull(rootBar);
        qnameFoo = QNameModule.create(URI.create("foo"), rev);
        qnameBar = QNameModule.create(URI.create("bar"), rev);
        assertNotNull(qnameFoo);
        assertNotNull(qnameBar);
    }

    @Test
    public void caseTest() {
        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fff"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ffn"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fnf"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nff"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nnf"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nfn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fnn"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ttt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ttn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nnt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tff"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnf"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tfn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntf"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());
    }

    @Test
    public void shortCaseTest() {
        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fff"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ffn"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fnf"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nff"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nnf"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nfn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fnn"));
        assertNotNull(tempChild);
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ttt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ttn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nnt"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tff"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnf"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tfn"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntf"));
        assertNotNull(tempChild);
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().values().iterator().next();
        assertNotNull(tempChoice);
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertFalse(tempThirdChild.isConfiguration());
    }

    @Test
    public void testInferenceExceptionChoice() throws Exception {
        expectedEx.expect(ReactorException.class);
        schema = StmtTestUtils.parseYangSources("/case-test/case-test-exceptions/choice");
    }

    @Test
    public void testInferenceExceptionCase() throws Exception {
        expectedEx.expect(ReactorException.class);
        schema = StmtTestUtils.parseYangSources("/case-test/case-test-exceptions/case");
    }
}