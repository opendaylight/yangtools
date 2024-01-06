/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.odlext.model.api.ContextInstanceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.ResourceYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

class ContextReferenceTest {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"));
    private static final QName LEAF_TYPE = QName.create(FOO, "leaf-type");
    private static final QName LIST_TYPE = QName.create(FOO, "list-type");

    @Test
    void test() throws Exception {
        final var reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new ContextInstanceStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new ContextReferenceStatementSupport(YangParserConfiguration.DEFAULT))
            .build();

        final var foo = reactor.newBuild()
            .addSource(YangStatementStreamSource.create(new ResourceYangTextSource(
                ContextReferenceTest.class, "/yang-ext.yang")))
            .addSource(YangStatementStreamSource.create(new ResourceYangTextSource(
                ContextReferenceTest.class, "/ctxref.yang")))
            .buildEffective()
            .getModuleStatements()
            .get(FOO);

        final var list = assertInstanceOf(ListEffectiveStatement.class,
            foo.findDataTreeNode(QName.create(FOO, "list")).orElseThrow());

        final var listType = list.findFirstEffectiveSubstatement(ContextInstanceEffectiveStatement.class).orElseThrow();
        assertEquals(LIST_TYPE, listType.argument());
        assertEquals(LIST_TYPE, listType.contextType().argument());

        final var leafType = list
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContextInstanceEffectiveStatement.class).orElseThrow();
        assertEquals(LEAF_TYPE, leafType.argument());
        assertEquals(LEAF_TYPE, leafType.contextType().argument());

        final var groupings = foo.streamEffectiveSubstatements(GroupingEffectiveStatement.class).toList();
        assertEquals(2, groupings.size());

        final var listRef = groupings.get(1)
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContextReferenceEffectiveStatement.class).orElseThrow();
        assertEquals(LIST_TYPE, listType.argument());
        assertSame(listType.contextType(), listRef.contextType());

        final var leafRef = groupings.get(0)
            .findFirstEffectiveSubstatement(LeafListEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContextReferenceEffectiveStatement.class).orElseThrow();
        assertEquals(LEAF_TYPE, leafType.argument());
        assertSame(leafType.contextType(), leafRef.contextType());
    }
}
