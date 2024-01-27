/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

class Bug4231Test {
    private static final QNameModule FOO = QNameModule.of("foo", "2015-09-02");

    @Test
    void test() throws Exception {
        assertInstanceOf(LeafListEffectiveStatement.class, TestUtils.parseYangSource("/bugs/bug4231.yang")
            .getModuleStatement(FOO)
            .findSchemaTreeNode(QName.create(FOO, "augment-target"), QName.create(FOO, "my-container-in-grouping"),
                QName.create(FOO, "l2"))
            .orElseThrow());
    }
}
