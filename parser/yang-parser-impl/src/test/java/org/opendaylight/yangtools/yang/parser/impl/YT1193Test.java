/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

public class YT1193Test {
    @Test
    public void testDeclarationReference() throws Exception {
        final List<DeclaredStatement<?>> declaredRoots = new DefaultYangParserFactory()
            .createParser(YangParserConfiguration.builder().retainDeclarationReferences(true).build())
            .addSource(YangTextSchemaSource.forResource(getClass(), "/yt1193/foo.yang"))
            .addSource(YangTextSchemaSource.forResource(getClass(), "/yt1193/bar.yang"))
            .addSource(YangTextSchemaSource.forResource(getClass(), "/yt1193/baz.yang"))
            .buildDeclaredModel();
        assertEquals(3, declaredRoots.size());

        assertFooDeclarationReferences(declaredRoots.get(0));
        assertBarDeclarationReferences(declaredRoots.get(1));
        assertBazDeclarationReferences(declaredRoots.get(2));



    }

    private static void assertFooDeclarationReferences(final DeclaredStatement<?> declaredStatement) {
        // TODO Auto-generated method stub

    }

    private static void assertBarDeclarationReferences(final DeclaredStatement<?> declaredStatement) {
        // TODO Auto-generated method stub

    }

    private static void assertBazDeclarationReferences(final DeclaredStatement<?> declaredStatement) {
        // TODO Auto-generated method stub

    }
}
