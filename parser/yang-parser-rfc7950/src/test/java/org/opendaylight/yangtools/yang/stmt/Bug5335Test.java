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
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug5335Test {
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
    public void incorrectTest1() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-1"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void incorrectTest2() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-2"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void incorrectTest3() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug5335/incorrect/case-2"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'mandatory-leaf' because it is mandatory and in module different than target"));
    }

    @Test
    public void correctTest1() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-1");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, PRESENCE_CONTAINER_B, MANDATORY_LEAF_B)
            .orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void correctTest2() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-2");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, PRESENCE_CONTAINER_B,
            NON_PRESENCE_CONTAINER_B, MANDATORY_LEAF_B).orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void correctTest3() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-3");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, PRESENCE_CONTAINER_B,
            NON_PRESENCE_CONTAINER_B, MANDATORY_LEAF_B).orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void correctTest4() throws Exception {
        final EffectiveModelContext context = StmtTestUtils.parseYangSources("/bugs/bug5335/correct/case-4");
        final DataSchemaNode mandatoryLeaf = context.findDataTreeChild(ROOT, NON_PRESENCE_CONTAINER_F, MANDATORY_LEAF_F)
            .orElse(null);
        assertThat(mandatoryLeaf, instanceOf(LeafSchemaNode.class));
    }
}
