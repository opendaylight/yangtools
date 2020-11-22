/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
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
    private CaseSchemaNode tempChoice;

    @Before
    public void setup() throws Exception {
        schema = StmtTestUtils.parseYangSources("/case-test");
        Revision rev = Revision.of("2015-09-09");
        rootFoo = schema.findModule("foo", rev).get();
        rootBar = schema.findModule("bar", rev).get();
        qnameFoo = QNameModule.create(URI.create("foo"), rev);
        qnameBar = QNameModule.create(URI.create("bar"), rev);
    }

    @Test
    public void caseTest() {
        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fff"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ffn"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fnf"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nff"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nnf"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nfn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fnn"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ttt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ttn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nnt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tff"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnf"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tfn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntf"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());
    }

    @Test
    public void shortCaseTest() {
        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fff"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ffn"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fnf"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nff"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nnf"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nfn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fnn"));
        assertFalse(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ttt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ttn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nnt"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertTrue(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertTrue(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tff"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnf"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tfn"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertFalse(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntf"));
        assertTrue(tempChild.isConfiguration());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertTrue(tempSecondChild.isConfiguration());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertFalse(tempChoice.isConfiguration());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertFalse(tempThirdChild.isConfiguration());
    }

    @Test
    public void testInferenceExceptionChoice() {
        assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/case-test/case-test-exceptions/choice"));
    }

    @Test
    public void testInferenceExceptionCase() throws Exception {
        assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/case-test/case-test-exceptions/case"));
    }
}