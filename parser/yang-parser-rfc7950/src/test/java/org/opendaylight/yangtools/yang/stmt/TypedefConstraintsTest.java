/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Range;
import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

public class TypedefConstraintsTest extends AbstractYangTest {
    @Test
    public void decimalRangeConstraintsTest() {
        final var context = assertEffectiveModelDir("/stmt-test/constraints");

        assertNotNull(context);

        final Collection<? extends TypeDefinition<?>> typeDefinitions = context.getTypeDefinitions();
        assertNotNull(typeDefinitions);
        assertEquals(1, typeDefinitions.size());

        final TypeDefinition<?> myDecimal = typeDefinitions.iterator().next();

        assertNotNull(myDecimal);
        assertTrue(myDecimal instanceof DecimalTypeDefinition);

        final Set<? extends Range<?>> rangeConstraints = ((DecimalTypeDefinition) myDecimal).getRangeConstraint()
                .get().getAllowedRanges().asRanges();

        assertNotNull(rangeConstraints);
        assertEquals(1, rangeConstraints.size());

        final DataSchemaNode dataNode = context.getDataChildByName(QName.create("urn:opendaylight.foo", "2013-10-08",
            "id-decimal64"));
        assertNotNull(dataNode);
        assertTrue(dataNode instanceof LeafSchemaNode);

        final LeafSchemaNode leafDecimal = (LeafSchemaNode) dataNode;
        final TypeDefinition<?> type = leafDecimal.getType();

        assertTrue(type instanceof DecimalTypeDefinition);
        final DecimalTypeDefinition decType = (DecimalTypeDefinition) type;
        assertEquals(4, decType.getFractionDigits());

        final Set<? extends Range<Decimal64>> decRangeConstraints = decType.getRangeConstraint().get()
                .getAllowedRanges().asRanges();

        assertEquals(1, decRangeConstraints.size());

        final Range<Decimal64> range = decRangeConstraints.iterator().next();
        assertEquals(Decimal64.of(4, 15000), range.lowerEndpoint());
        assertEquals(4, range.lowerEndpoint().scale());
        assertEquals(Decimal64.of(4, 55000), range.upperEndpoint());
        assertEquals(4, range.upperEndpoint().scale());

        assertEquals(TypeDefinitions.DECIMAL64.bindTo(leafDecimal.getQName().getModule()), decType.getQName());
        assertNull(decType.getBaseType());
    }
}
