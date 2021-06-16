/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.odlext.model.api.ContextInstanceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class AugmentEquivalentTest {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"));
    private static final QName LEAF_TYPE = QName.create(FOO, "leaf-type");
    private static final QName LIST_TYPE = QName.create(FOO, "list-type");

    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new AugmentIdentifierStatementSupport(YangParserConfiguration.DEFAULT))
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new AugmentEquivalentStatementSupport(YangParserConfiguration.DEFAULT))
            .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void test() throws Exception {
        final ModuleEffectiveStatement foo = reactor.newBuild()
            .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/yang-ext.yang")))
            .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/augment-equiv.yang")))
            .buildEffective()
            .getModuleStatements()
            .get(FOO);

        final DataTreeEffectiveStatement<?> list = foo.findDataTreeNode(QName.create(FOO, "list")).orElseThrow();
        assertThat(list, instanceOf(ListEffectiveStatement.class));

        final ContextInstanceEffectiveStatement listType = list
            .findFirstEffectiveSubstatement(ContextInstanceEffectiveStatement.class).orElseThrow();
        assertEquals(LIST_TYPE, listType.argument());
        assertEquals(LIST_TYPE, listType.contextType().argument());

        final ContextInstanceEffectiveStatement leafType = list
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContextInstanceEffectiveStatement.class).orElseThrow();
        assertEquals(LEAF_TYPE, leafType.argument());
        assertEquals(LEAF_TYPE, leafType.contextType().argument());

        final List<GroupingEffectiveStatement> groupings = foo
            .streamEffectiveSubstatements(GroupingEffectiveStatement.class).collect(Collectors.toList());
        assertEquals(2, groupings.size());

        final ContextReferenceEffectiveStatement listRef = groupings.get(1)
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContextReferenceEffectiveStatement.class).orElseThrow();
        assertEquals(LIST_TYPE, listType.argument());
        assertSame(listType.contextType(), listRef.contextType());

        final ContextReferenceEffectiveStatement leafRef = groupings.get(0)
            .findFirstEffectiveSubstatement(LeafListEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContextReferenceEffectiveStatement.class).orElseThrow();
        assertEquals(LEAF_TYPE, leafType.argument());
        assertSame(leafType.contextType(), leafRef.contextType());
    }
}
