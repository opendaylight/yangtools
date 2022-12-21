/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

class Bug4231Test {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"), Revision.of("2015-09-02"));

    @Test
    void test() throws Exception {
        final SchemaTreeEffectiveStatement<?> stmt = TestUtils.parseYangSource("/bugs/bug4231.yang")
            .getModuleStatement(FOO)
            .findSchemaTreeNode(QName.create(FOO, "augment-target"), QName.create(FOO, "my-container-in-grouping"),
                QName.create(FOO, "l2"))
            .orElse(null);
        assertThat(stmt, instanceOf(LeafListEffectiveStatement.class));
    }
}
