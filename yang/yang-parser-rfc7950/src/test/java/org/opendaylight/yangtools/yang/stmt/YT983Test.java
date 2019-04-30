/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class YT983Test {
    private static final QName FOO = QName.create("foo", "2019-04-30", "foo");

    @Test
    public void testAugmentationConfig() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSource("/bugs/YT983/foo.yang");
        final DataSchemaNode foo = context.findDataChildByName(FOO).get();
        assertTrue(foo instanceof LeafSchemaNode);
    }
}
