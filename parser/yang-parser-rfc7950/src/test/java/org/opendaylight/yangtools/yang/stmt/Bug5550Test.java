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
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class Bug5550Test extends AbstractYangTest {
    private static final String NS = "foo";
    private static final String REV = "2016-03-18";

    @Test
    void test() {
        assertInstanceOf(LeafSchemaNode.class, assertEffectiveModelDir("/bugs/bug5550")
            .findDataTreeChild(
                QName.create(NS, REV, "root"),
                QName.create(NS, REV, "container-in-grouping"),
                QName.create(NS, REV, "leaf-1"))
            .orElseThrow());
    }
}
