/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.DerivedTypes;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.model.ri.type.StringTypeBuilder;

public class BindingGeneratorUtilTest {
    private static final QName ROOT = QName.create("test", "root");

    @Test
    public void getRestrictionsTest() throws InvalidLengthConstraintException {
        final PatternConstraint constraint = mock(PatternConstraint.class);

        final StringTypeBuilder builder =
                RestrictedTypes.newStringBuilder(BaseTypes.stringType(), ROOT);

        builder.addPatternConstraint(constraint);
        builder.setLengthConstraint(mock(ConstraintMetaDefinition.class), List.of(ValueRange.of(1, 2)));

        Restrictions restrictions = BindingGeneratorUtil.getRestrictions(builder.build());

        assertNotNull(restrictions);
        assertEquals(Set.of(Range.closed(1, 2)),
            restrictions.getLengthConstraint().orElseThrow().getAllowedRanges().asRanges());
        assertFalse(restrictions.getRangeConstraint().isPresent());
        assertEquals(1, restrictions.getPatternConstraints().size());

        assertFalse(restrictions.isEmpty());
        assertThat(restrictions.getPatternConstraints(), contains(constraint));
    }

    @Test
    public void getEmptyRestrictionsTest() {
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(BaseTypes.stringType(), ROOT).build();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());
    }

    @Test
    public void getDefaultIntegerRestrictionsTest() {
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(BaseTypes.int16Type(), ROOT).build();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((Int16TypeDefinition) type.getBaseType()).getRangeConstraint(),
                restrictions.getRangeConstraint());
        assertEquals(Optional.empty(), restrictions.getLengthConstraint());
        assertEquals(List.of(), restrictions.getPatternConstraints());
    }

    @Test
    public void getDefaultUnsignedIntegerRestrictionsTest() {
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(BaseTypes.uint16Type(), ROOT).build();
        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(((Uint16TypeDefinition) type.getBaseType()).getRangeConstraint(),
                restrictions.getRangeConstraint());
        assertEquals(Optional.empty(), restrictions.getLengthConstraint());
        assertEquals(List.of(), restrictions.getPatternConstraints());
    }

    @Test
    public void getDefaultDecimalRestrictionsTest() {
        final DecimalTypeDefinition base = BaseTypes.decimalTypeBuilder(ROOT).setFractionDigits(10).build();
        final TypeDefinition<?> type = DerivedTypes.derivedTypeBuilder(base, ROOT).build();

        final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(base.getRangeConstraint(), restrictions.getRangeConstraint());
        assertEquals(Optional.empty(), restrictions.getLengthConstraint());
        assertEquals(List.of(), restrictions.getPatternConstraints());
    }

    @Test
    public void unicodeCharReplaceTest() {
        String inputString = "abcu\\uuuuu\\uuua\\u\\\\uabc\\\\uuuu\\\\\\uuuu\\\\\\\\uuuu///uu/u/u/u/u/u/u";

        assertEquals("abcu\\\\uuuuu\\\\uuua\\\\u\\\\uabc\\\\uuuu\\\\uuuu\\\\uuuu///uu/u/u/u/u/u/u",
            BindingGeneratorUtil.replaceAllIllegalChars(inputString));
    }
}
