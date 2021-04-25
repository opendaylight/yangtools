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
import static org.junit.Assert.assertThrows;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug8126Test {
    private static final XMLNamespace FOO_NS = XMLNamespace.of("foo");
    private static final XMLNamespace BAR_NS = XMLNamespace.of("bar");

    @Test
    public void testValidAugments() throws Exception {
        final ModuleEffectiveStatement fooModule = StmtTestUtils.parseYangSources("/bugs/bug8126/valid")
            .getModuleStatement(QNameModule.create(FOO_NS));
        assertThat(fooModule.findSchemaTreeNode(
            foo("root"), bar("my-container"), bar("my-choice"), bar("one"), bar("one"), bar("mandatory-leaf"))
            .orElse(null), instanceOf(LeafSchemaNode.class));
        assertThat(fooModule.findSchemaTreeNode(foo("root"), bar("my-list"), bar("two"), bar("mandatory-leaf-2"))
            .orElse(null), instanceOf(LeafSchemaNode.class));
        assertEquals(Optional.empty(), fooModule.findSchemaTreeNode(foo("root"), bar("mandatory-list")));
        assertEquals(Optional.empty(), fooModule.findSchemaTreeNode(
            foo("root"), bar("mandatory-container"), bar("mandatory-choice")));
        assertEquals(Optional.empty(), fooModule.findSchemaTreeNode(
            foo("root"), bar("mandatory-container-2"), bar("one"), bar("mandatory-leaf-3")));
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

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, localName);
    }
}
