/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInText;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

public class YT1193Test {
    @Test
    public void testDeclarationReference() throws Exception {
        final List<DeclaredStatement> declaredRoots = new DefaultYangParserFactory()
            .createParser(YangParserConfiguration.builder().retainDeclarationReferences(true).build())
            .addSource(YangTextSchemaSource.forResource(getClass(), "/yt1193/foo.yang"))
            .addSource(YangTextSchemaSource.forResource(getClass(), "/yt1193/bar.yang"))
            .addSource(YangTextSchemaSource.forResource(getClass(), "/yt1193/baz.yang"))
            .buildDeclaredModel();
        assertEquals(3, declaredRoots.size());

        for (DeclaredStatement stmt : declaredRoots) {
            switch (stmt.rawArgument()) {
                case "foo":
                    assertFooReferences(stmt);
                    break;
                case "bar":
                    assertBarReferences(stmt);
                    break;
                case "baz":
                    assertBazReferences(stmt);
                    break;
                default:
                    throw new IllegalStateException("Unexpected statement " + stmt);
            }
        }
    }

    private static void assertFooReferences(final DeclaredStatement foo) {
        assertReference(foo, YangStmtMapping.MODULE, 1, 1);

        final Iterator<? extends DeclaredStatement> it = foo.declaredSubstatements().iterator();
        assertReference(it.next(), YangStmtMapping.NAMESPACE, 2, 3);
        assertReference(it.next(), YangStmtMapping.PREFIX, 3, 3);
        assertReference(it.next(), YangStmtMapping.YANG_VERSION, 4, 3);
        assertReference(it.next(), YangStmtMapping.REVISION, 6, 3);
        assertReference(it.next(), YangStmtMapping.ORGANIZATION, 8, 3);
        assertReference(it.next(), YangStmtMapping.DESCRIPTION, 9, 3);
        assertReference(it.next(), YangStmtMapping.REFERENCE, 10, 3);
        assertReference(it.next(), YangStmtMapping.CONTACT, 11, 3);
        assertFooContainerReferences(it.next());
        assertReference(it.next(), YangStmtMapping.RPC, 25, 3);
        assertReference(it.next(), YangStmtMapping.NOTIFICATION, 26, 3);
        assertDeprLeafListReferences(it.next());
        assertObsoTypedefReferences(it.next());
        assertFalse(it.hasNext());
    }

    private static void assertFooContainerReferences(final DeclaredStatement foo) {
        assertReference(foo, YangStmtMapping.CONTAINER, 13, 3);

        final Iterator<? extends DeclaredStatement> it = foo.declaredSubstatements().iterator();
        assertReference(it.next(), YangStmtMapping.ACTION, 14, 5);
        assertReference(it.next(), YangStmtMapping.PRESENCE, 22, 5);
        assertFalse(it.hasNext());
    }

    private static void assertDeprLeafListReferences(final DeclaredStatement depr) {
        assertReference(depr, YangStmtMapping.LEAF_LIST, 28, 3);

        final Iterator<? extends DeclaredStatement> it = depr.declaredSubstatements().iterator();
        assertReference(it.next(), YangStmtMapping.TYPE, 29, 5);
        assertReference(it.next(), YangStmtMapping.UNITS, 36, 5);
        assertReference(it.next(), YangStmtMapping.STATUS, 37, 5);
        assertFalse(it.hasNext());
    }

    private static void assertObsoTypedefReferences(final DeclaredStatement obso) {
        assertReference(obso, YangStmtMapping.TYPEDEF, 40, 3);

        final Iterator<? extends DeclaredStatement> it = obso.declaredSubstatements().iterator();
        assertReference(it.next(), YangStmtMapping.TYPE, 41, 5);
        assertReference(it.next(), YangStmtMapping.STATUS, 44, 5);
        assertFalse(it.hasNext());
    }

    private static void assertBarReferences(final DeclaredStatement bar) {
        assertReference(bar, YangStmtMapping.MODULE, 1, 1);

        final Iterator<? extends DeclaredStatement> it = bar.declaredSubstatements().iterator();
        assertReference(it.next(), YangStmtMapping.NAMESPACE, 2, 3);
        assertReference(it.next(), YangStmtMapping.PREFIX, 3, 3);
        assertReference(it.next(), YangStmtMapping.YANG_VERSION, 4, 3);
        assertReference(it.next(), YangStmtMapping.IMPORT, 6, 3);
        assertReference(it.next(), YangStmtMapping.IDENTITY, 11, 3);
        assertReference(it.next(), YangStmtMapping.IDENTITY, 13, 3);
        assertReference(it.next(), YangStmtMapping.ANYDATA, 17, 3);
        assertReference(it.next(), YangStmtMapping.ANYXML, 18, 3);
        assertReference(it.next(), YangStmtMapping.INCLUDE, 20, 3);
        assertFalse(it.hasNext());
    }

    private static void assertBazReferences(final DeclaredStatement baz) {
        assertReference(baz, YangStmtMapping.SUBMODULE, 1, 1);

        final Iterator<? extends DeclaredStatement> it = baz.declaredSubstatements().iterator();
        assertReference(it.next(), YangStmtMapping.YANG_VERSION, 2, 3);
        assertReference(it.next(), YangStmtMapping.BELONGS_TO, 4, 3);
        assertReference(it.next(), YangStmtMapping.EXTENSION, 8, 3);
        assertFalse(it.hasNext());
    }

    private static void assertReference(final DeclaredStatement foo, final StatementDefinition def, final int line,
            final int column) {
        assertEquals(def, foo.statementDefinition());

        final DeclarationReference ref = foo.declarationReference().orElseThrow();
        assertThat(ref, instanceOf(DeclarationInText.class));
        final DeclarationInText inText = (DeclarationInText) ref;

        assertEquals(line, inText.startLine());
        assertEquals(column, inText.startColumn());
    }
}
