/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

class YT841Test extends AbstractYangTest {
    private static final QName FOO = QName.create("foo", "2018-01-02", "foo");

    @Test
    void testFindDataSchemaNode() throws Exception {
        final SchemaTreeEffectiveStatement<?> input = assertEffectiveModelDir("/bugs/YT841/")
            .getModuleStatement(FOO)
            .findSchemaTreeNode(FOO, FOO, FOO, QName.create(FOO, "input"))
            .orElse(null);
        assertThat(input, instanceOf(InputEffectiveStatement.class));
    }
}
