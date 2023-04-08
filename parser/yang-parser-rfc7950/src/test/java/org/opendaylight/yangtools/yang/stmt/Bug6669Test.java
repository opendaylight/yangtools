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

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class Bug6669Test extends AbstractYangTest {
    private static final String REV = "2016-09-08";
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";
    private static final QName ROOT = QName.create(FOO_NS, REV, "root");
    private static final QName BAR = QName.create(BAR_NS, REV, "bar");
    private static final QName BAR_1 = QName.create(BAR_NS, REV, "bar1");
    private static final QName BAR_2 = QName.create(BAR_NS, REV, "bar2");
    private static final QName M = QName.create(BAR_NS, REV, "m");

    @Test
    public void testInvalidAugment() {
        assertInferenceExceptionDir("/bugs/bug6669/invalid/test1", startsWith(
            "An augment cannot add node 'm' because it is mandatory and in module different than target"));
    }

    @Test
    public void testInvalidAugment2() {
        assertInferenceExceptionDir("/bugs/bug6669/invalid/test2", startsWith(
            "An augment cannot add node 'm' because it is mandatory and in module different than target"));
    }

    @Test
    public void testInvalidAugment3() {
        assertInferenceExceptionDir("/bugs/bug6669/invalid/test3", startsWith(
            "An augment cannot add node 'l' because it is mandatory and in module different than target"));
    }

    @Test
    public void testValidAugment() {
        final var context = assertEffectiveModelDir("/bugs/bug6669/valid/test1");
        final var findDataSchemaNode = context.findDataTreeChild(ROOT, BAR, BAR_1, M).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void testValidAugment2() {
        final var context = assertEffectiveModelDir("/bugs/bug6669/valid/test2");
        final var findDataSchemaNode = context.findDataTreeChild(ROOT, BAR, BAR_1, BAR_2, M).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void testValidAugment3() throws Exception {
        final var context = assertEffectiveModelDir("/bugs/bug6669/valid/test3");
        final var findDataSchemaNode = context.findDataTreeChild(ROOT, BAR, BAR_1, BAR_2,
                QName.create(BAR_NS, REV, "l")).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(ListSchemaNode.class));
    }
}
