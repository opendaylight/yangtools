/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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

class YT983Test extends AbstractYangTest {
    private static final QName FOO = QName.create("foo", "2019-04-30", "foo");

    @Test
    void testAugmentationConfig() {
        final var context = assertEffectiveModel("/bugs/YT983/foo.yang");
        assertInstanceOf(LeafSchemaNode.class, context.getDataChildByName(FOO));
    }
}
