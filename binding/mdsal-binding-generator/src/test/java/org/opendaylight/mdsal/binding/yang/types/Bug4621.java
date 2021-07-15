/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4621 {
    @Test
    public void bug4621test() {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug4621.yang");
        final Module moduleValid = schemaContext.findModules(XMLNamespace.of("foo")).iterator().next();
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final QName listNode = QName.create(moduleValid.getQNameModule(), "neighbor");
        final QName leafrefNode = QName.create(moduleValid.getQNameModule(), "neighbor2-id");
        final DataSchemaNode leafrefRel = ((ListSchemaNode) moduleValid.getDataChildByName(listNode))
                .getDataChildByName(leafrefNode);
        final LeafSchemaNode leafRel = (LeafSchemaNode) leafrefRel;
        final TypeDefinition<?> leafTypeRel = leafRel.getType();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> typeProvider.javaTypeForSchemaDefinitionType(leafTypeRel, leafRel));
        assertThat(ex.getMessage(),
            startsWith("Failed to find leafref target: /foo:neighbor/foo:mystring1 in module foo"));
    }
}