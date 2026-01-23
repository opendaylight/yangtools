/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationInText;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.impl.dagger.DaggerYangParserComponent;

class YT1193Test {
    @Test
    void testDeclarationReference() throws Exception {
        final var declaredRoots = DaggerYangParserComponent.create()
            .parserFactory()
            .createParser(YangParserConfiguration.builder().retainDeclarationReferences(true).build())
            .addSource(new URLYangTextSource(YT1193Test.class.getResource("/yt1193/foo.yang")))
            .addSource(new URLYangTextSource(YT1193Test.class.getResource("/yt1193/bar.yang")))
            .addSource(new URLYangTextSource(YT1193Test.class.getResource("/yt1193/baz.yang")))
            .buildDeclaredModel();
        assertEquals(3, declaredRoots.size());

        for (var stmt : declaredRoots) {
            switch (stmt.rawArgument()) {
                case "foo" -> assertFooReferences(stmt);
                case "bar" -> assertBarReferences(stmt);
                case "baz" -> assertBazReferences(stmt);
                default -> throw new IllegalStateException("Unexpected statement " + stmt);
            }
        }
    }

    private static void assertFooReferences(final DeclaredStatement<?> foo) {
        assertReference(foo, ModuleStatement.DEF, 1, 1);

        final var it = foo.declaredSubstatements().iterator();
        assertReference(it.next(), NamespaceStatement.DEF, 2, 3);
        assertReference(it.next(), PrefixStatement.DEF, 3, 3);
        assertReference(it.next(), YangVersionStatement.DEF, 4, 3);
        assertReference(it.next(), RevisionStatement.DEF, 6, 3);
        assertReference(it.next(), OrganizationStatement.DEF, 8, 3);
        assertReference(it.next(), DescriptionStatement.DEF, 9, 3);
        assertReference(it.next(), ReferenceStatement.DEF, 10, 3);
        assertReference(it.next(), ContactStatement.DEF, 11, 3);
        assertFooContainerReferences(it.next());
        assertReference(it.next(), RpcStatement.DEF, 25, 3);
        assertReference(it.next(), NotificationStatement.DEF, 26, 3);
        assertDeprLeafListReferences(it.next());
        assertObsoTypedefReferences(it.next());
        assertFalse(it.hasNext());
    }

    private static void assertFooContainerReferences(final DeclaredStatement<?> foo) {
        assertReference(foo, ContainerStatement.DEF, 13, 3);

        final var it = foo.declaredSubstatements().iterator();
        assertReference(it.next(), ActionStatement.DEF, 14, 5);
        assertReference(it.next(), PresenceStatement.DEF, 22, 5);
        assertFalse(it.hasNext());
    }

    private static void assertDeprLeafListReferences(final DeclaredStatement<?> depr) {
        assertReference(depr, LeafListStatement.DEF, 28, 3);

        final var it = depr.declaredSubstatements().iterator();
        assertReference(it.next(), TypeStatement.DEF, 29, 5);
        assertReference(it.next(), UnitsStatement.DEF, 36, 5);
        assertReference(it.next(), StatusStatement.DEF, 37, 5);
        assertFalse(it.hasNext());
    }

    private static void assertObsoTypedefReferences(final DeclaredStatement<?> obso) {
        assertReference(obso, TypedefStatement.DEF, 40, 3);

        final var it = obso.declaredSubstatements().iterator();
        assertReference(it.next(), TypeStatement.DEF, 41, 5);
        assertReference(it.next(), StatusStatement.DEF, 44, 5);
        assertFalse(it.hasNext());
    }

    private static void assertBarReferences(final DeclaredStatement<?> bar) {
        assertReference(bar, ModuleStatement.DEF, 1, 1);

        final var it = bar.declaredSubstatements().iterator();
        assertReference(it.next(), NamespaceStatement.DEF, 2, 3);
        assertReference(it.next(), PrefixStatement.DEF, 3, 3);
        assertReference(it.next(), YangVersionStatement.DEF, 4, 3);
        assertReference(it.next(), ImportStatement.DEF, 6, 3);
        assertReference(it.next(), IdentityStatement.DEF, 11, 3);
        assertReference(it.next(), IdentityStatement.DEF, 13, 3);
        assertReference(it.next(), AnydataStatement.DEF, 17, 3);
        assertReference(it.next(), AnyxmlStatement.DEF, 18, 3);
        assertReference(it.next(), IncludeStatement.DEF, 20, 3);
        assertFalse(it.hasNext());
    }

    private static void assertBazReferences(final DeclaredStatement<?> baz) {
        assertReference(baz, SubmoduleStatement.DEF, 1, 1);

        final var it = baz.declaredSubstatements().iterator();
        assertReference(it.next(), YangVersionStatement.DEF, 2, 3);
        assertReference(it.next(), BelongsToStatement.DEF, 4, 3);
        assertReference(it.next(), ExtensionStatement.DEF, 8, 3);
        assertFalse(it.hasNext());
    }

    private static void assertReference(final DeclaredStatement<?> foo, final StatementDefinition def, final int line,
            final int column) {
        assertEquals(def, foo.statementDefinition());

        final var inText = assertInstanceOf(DeclarationInText.class, foo.declarationReference().orElseThrow());
        assertEquals(line, inText.startLine());
        assertEquals(column, inText.startColumn());
    }
}
