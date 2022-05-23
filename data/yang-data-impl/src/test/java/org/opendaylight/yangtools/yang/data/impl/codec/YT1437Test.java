/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1437Test {
    @Test
    public void testDecimalFractionDigits() {
        final Module module = YangParserTestUtils.parseYangResource("/yt1437.yang").findModule("yt1437").orElseThrow();
        final DataSchemaNode foo = module.findDataChildByName(QName.create(module.getQNameModule(), "foo"))
                .orElseThrow();
        assertThat(foo, instanceOf(LeafSchemaNode.class));

        final TypeDefinitionAwareCodec<?, ?> codec = TypeDefinitionAwareCodec.from(((LeafSchemaNode) foo).getType());
        assertThat(codec, instanceOf(DecimalStringCodec.class));
    }
}
