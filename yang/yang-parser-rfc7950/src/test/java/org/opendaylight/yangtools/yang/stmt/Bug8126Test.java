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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug8126Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";

    @Test
    public void testValidAugments() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug8126/valid");
        assertThat(findNode(context, foo("root"), bar("my-container"), bar("my-choice"), bar("one"), bar("one"),
                bar("mandatory-leaf")), instanceOf(LeafSchemaNode.class));
        assertThat(context.findDataTreeChild(foo("root"), bar("my-list"), bar("two"), bar("mandatory-leaf-2")).get(),
            instanceOf(LeafSchemaNode.class));
        assertEquals(Optional.empty(), context.findDataTreeChild(foo("root"), bar("mandatory-list")));
        assertNull(findNode(context, foo("root"), bar("mandatory-container"), bar("mandatory-choice")));
        assertEquals(Optional.empty(), context.findDataTreeChild(foo("root"), bar("mandatory-container-2"), bar("one"),
                bar("mandatory-leaf-3")));
    }

    @Test
    public void testAugmentMandatoryChoice() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug8126/inv-choice"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'mandatory-choice' because it is mandatory and in module different than "));
    }

    @Test
    public void testAugmentMandatoryList() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug8126/inv-list"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'mandatory-list' because it is mandatory and in module different than "));
    }

    @Test
    public void testAugmentMandatoryContainer() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug8126/inv-cont"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(InferenceException.class));
        assertThat(cause.getMessage(), startsWith(
            "An augment cannot add node 'mandatory-leaf-3' because it is mandatory and in module different than "));
    }

    private static SchemaNode findNode(final SchemaContext context, final QName... qnames) {
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, qnames));
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, localName);
    }
}
