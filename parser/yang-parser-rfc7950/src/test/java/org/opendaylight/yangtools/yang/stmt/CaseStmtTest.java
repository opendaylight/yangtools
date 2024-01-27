/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

class CaseStmtTest extends AbstractYangTest {
    private static final Optional<Boolean> OPT_FALSE = Optional.of(Boolean.FALSE);
    private static final Optional<Boolean> OPT_TRUE = Optional.of(Boolean.TRUE);

    private EffectiveModelContext schema;
    private Module rootFoo;
    private Module rootBar;
    private QNameModule qnameFoo;
    private QNameModule qnameBar;
    private DataSchemaNode tempChild;
    private DataSchemaNode tempSecondChild;
    private DataSchemaNode tempThirdChild;
    private CaseSchemaNode tempChoice;

    @BeforeEach
    void setup() {
        schema = assertEffectiveModelDir("/case-test");
        Revision rev = Revision.of("2015-09-09");
        rootFoo = schema.findModule("foo", rev).orElseThrow();
        rootBar = schema.findModule("bar", rev).orElseThrow();
        assertNotNull(rootFoo);
        assertNotNull(rootBar);
        qnameFoo = QNameModule.of(XMLNamespace.of("foo"), rev);
        qnameBar = QNameModule.of(XMLNamespace.of("bar"), rev);
        assertNotNull(qnameFoo);
        assertNotNull(qnameBar);
    }

    @Test
    void caseTest() {
        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fff"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ffn"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fnf"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nff"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nnf"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(Optional.empty(), tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nfn"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-fnn"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ttt"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ttn"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnt"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntt"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-nnt"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(Optional.empty(), tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntn"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnn"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tff"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tnf"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-tfn"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootFoo.getDataChildByName(QName.create(qnameFoo, "root-ntf"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());
    }

    @Test
    void shortCaseTest() {
        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fff"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ffn"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fnf"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nff"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nnf"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(Optional.empty(), tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nfn"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-fnn"));
        assertNotNull(tempChild);
        assertEquals(OPT_FALSE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ttt"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ttn"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnt"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntt"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-nnt"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(Optional.empty(), tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntn"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnn"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_TRUE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_TRUE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tff"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tnf"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-tfn"));
        assertNotNull(tempChild);
        assertEquals(OPT_TRUE, tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_FALSE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());

        tempChild = rootBar.getDataChildByName(QName.create(qnameBar, "sh-root-ntf"));
        assertNotNull(tempChild);
        assertEquals(Optional.empty(), tempChild.effectiveConfig());
        tempSecondChild = ((ContainerSchemaNode) tempChild).getChildNodes().iterator().next();
        assertNotNull(tempSecondChild);
        assertEquals(OPT_TRUE, tempSecondChild.effectiveConfig());
        tempChoice = ((ChoiceSchemaNode) tempSecondChild).getCases().iterator().next();
        assertNotNull(tempChoice);
        assertEquals(OPT_FALSE, tempChoice.effectiveConfig());
        tempThirdChild = tempChoice.getChildNodes().iterator().next();
        assertNotNull(tempThirdChild);
        assertEquals(OPT_FALSE, tempThirdChild.effectiveConfig());
    }

    @Test
    void testInferenceExceptionChoice() {
        assertInferenceExceptionDir("/case-test/case-test-exceptions/choice",
            startsWith("Parent node has config=false, this node must not be specifed as config=true [at "));
    }

    @Test
    void testInferenceExceptionCase() {
        assertInferenceExceptionDir("/case-test/case-test-exceptions/case",
            startsWith("Parent node has config=false, this node must not be specifed as config=true [at "));
    }
}