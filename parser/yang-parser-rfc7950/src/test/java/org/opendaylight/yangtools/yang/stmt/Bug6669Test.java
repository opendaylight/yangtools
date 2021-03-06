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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug6669Test {
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
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug6669/invalid/test1"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'm' because it is mandatory and in module different than target"));
    }

    @Test
    public void testInvalidAugment2() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug6669/invalid/test2"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'm' because it is mandatory and in module different than target"));
    }

    @Test
    public void testInvalidAugment3() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () ->  StmtTestUtils.parseYangSources("/bugs/bug6669/invalid/test3"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'l' because it is mandatory and in module different than target"));
    }

    @Test
    public void testValidAugment() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/valid/test1");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = context.findDataTreeChild(ROOT, BAR, BAR_1, M).get();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void testValidAugment2() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/valid/test2");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = context.findDataTreeChild(ROOT, BAR, BAR_1, BAR_2, M).get();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));
    }

    @Test
    public void testValidAugment3() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6669/valid/test3");
        assertNotNull(context);

        final SchemaNode findDataSchemaNode = context.findDataTreeChild(ROOT, BAR, BAR_1, BAR_2,
                QName.create(BAR_NS, REV, "l")).get();
        assertThat(findDataSchemaNode, instanceOf(ListSchemaNode.class));
    }
}
