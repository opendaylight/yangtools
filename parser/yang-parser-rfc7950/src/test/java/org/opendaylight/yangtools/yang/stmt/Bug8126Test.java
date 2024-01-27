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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class Bug8126Test extends AbstractYangTest {
    private static final XMLNamespace FOO_NS = XMLNamespace.of("foo");
    private static final XMLNamespace BAR_NS = XMLNamespace.of("bar");

    @Test
    void testValidAugments() {
        final var fooModule = assertEffectiveModelDir("/bugs/bug8126/valid").getModuleStatement(QNameModule.of(FOO_NS));
        assertInstanceOf(LeafSchemaNode.class, fooModule.findSchemaTreeNode(
            foo("root"), bar("my-container"), bar("my-choice"), bar("one"), bar("one"), bar("mandatory-leaf"))
            .orElseThrow());
        assertInstanceOf(LeafSchemaNode.class, fooModule.findSchemaTreeNode(
            foo("root"), bar("my-list"), bar("two"), bar("mandatory-leaf-2"))
            .orElseThrow());
        assertEquals(Optional.empty(), fooModule.findSchemaTreeNode(foo("root"), bar("mandatory-list")));
        assertEquals(Optional.empty(), fooModule.findSchemaTreeNode(
            foo("root"), bar("mandatory-container"), bar("mandatory-choice")));
        assertEquals(Optional.empty(), fooModule.findSchemaTreeNode(
            foo("root"), bar("mandatory-container-2"), bar("one"), bar("mandatory-leaf-3")));
    }

    @Test
    void testAugmentMandatoryChoice() {
        assertInferenceExceptionDir("/bugs/bug8126/inv-choice", startsWith(
            "An augment cannot add node 'mandatory-choice' because it is mandatory and in module different than "));
    }

    @Test
    void testAugmentMandatoryList() {
        assertInferenceExceptionDir("/bugs/bug8126/inv-list", startsWith(
            "An augment cannot add node 'mandatory-list' because it is mandatory and in module different than "));
    }

    @Test
    void testAugmentMandatoryContainer() {
        assertInferenceExceptionDir("/bugs/bug8126/inv-cont", startsWith(
            "An augment cannot add node 'mandatory-leaf-3' because it is mandatory and in module different than "));
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, localName);
    }
}
