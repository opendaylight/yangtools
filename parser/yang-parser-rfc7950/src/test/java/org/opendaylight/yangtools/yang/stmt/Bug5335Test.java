/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class Bug5335Test extends AbstractYangTest {
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String REV = "2016-03-04";

    private static final QName ROOT = QName.create(FOO, REV, "root");
    private static final QName NON_PRESENCE_CONTAINER_F = QName.create(FOO, REV, "non-presence-container");
    private static final QName MANDATORY_LEAF_F = QName.create(FOO, REV, "mandatory-leaf");
    private static final QName PRESENCE_CONTAINER_B = QName.create(BAR, REV, "presence-container");
    private static final QName NON_PRESENCE_CONTAINER_B = QName.create(BAR, REV, "non-presence-container");
    private static final QName MANDATORY_LEAF_B = QName.create(BAR, REV, "mandatory-leaf");

    @Test
    void incorrectTest1() {
        assertInferenceExceptionDir("/bugs/bug5335/incorrect/case-1", startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    void incorrectTest2() {
        assertInferenceExceptionDir("/bugs/bug5335/incorrect/case-2", startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    void incorrectTest3() {
        assertInferenceExceptionDir("/bugs/bug5335/incorrect/case-3", startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    void correctTest1() {
        final EffectiveModelContext context = assertEffectiveModelDir("/bugs/bug5335/correct/case-1");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, PRESENCE_CONTAINER_B, MANDATORY_LEAF_B)
            .orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }

    @Test
    void correctTest2() {
        final EffectiveModelContext context = assertEffectiveModelDir("/bugs/bug5335/correct/case-2");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, PRESENCE_CONTAINER_B,
            NON_PRESENCE_CONTAINER_B, MANDATORY_LEAF_B).orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }

    @Test
    void correctTest3() {
        final EffectiveModelContext context = assertEffectiveModelDir("/bugs/bug5335/correct/case-3");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, PRESENCE_CONTAINER_B,
            NON_PRESENCE_CONTAINER_B, MANDATORY_LEAF_B).orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }

    @Test
    void correctTest4() {
        final EffectiveModelContext context = assertEffectiveModelDir("/bugs/bug5335/correct/case-4");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, NON_PRESENCE_CONTAINER_F, MANDATORY_LEAF_F)
            .orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }
}
