/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.DerivedTypes;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;

@ExtendWith(MockitoExtension.class)
class BindingGeneratorUtilTest {
    private static final QName ROOT = QName.create("test", "root");

    @Mock
    private PatternConstraint constraint;
    @Mock
    private ConstraintMetaDefinition constraintMeta;

    @Test
    void getRestrictionsTest() throws InvalidLengthConstraintException {
        final var builder = RestrictedTypes.newStringBuilder(BaseTypes.stringType(), ROOT);

        builder.addPatternConstraint(constraint);
        builder.setLengthConstraint(constraintMeta, ValueRanges.of(ValueRange.of(1, 2)));

        final var restrictions = BindingGeneratorUtil.getRestrictions(builder.build());

        assertNotNull(restrictions);
        assertEquals(Set.of(Range.closed(1, 2)),
            restrictions.getLengthConstraint().orElseThrow().getAllowedRanges().asRanges());
        assertFalse(restrictions.getRangeConstraint().isPresent());
        assertEquals(1, restrictions.getPatternConstraints().size());

        assertFalse(restrictions.isEmpty());
        assertThat(restrictions.getPatternConstraints()).contains(constraint);
    }

    @Test
    void getEmptyRestrictionsTest() {
        final var type = DerivedTypes.derivedTypeBuilder(BaseTypes.stringType(), ROOT).build();
        final var restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());
    }

    @Test
    void getRedundantRestrictionsTest() {
        final var builder = RestrictedTypes.newUint16Builder(BaseTypes.uint16Type(), ROOT);
        builder.setRangeConstraint(constraintMeta, ValueRanges.of(ValueRange.of(0, 65535)));
        final var restrictions = BindingGeneratorUtil.getRestrictions(builder.build());

        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());
        assertEquals(Optional.empty(), restrictions.getLengthConstraint());
        assertEquals(List.of(), restrictions.getPatternConstraints());
    }

    @Test
    void getDefaultIntegerRestrictionsTest() {
        final var type = DerivedTypes.derivedTypeBuilder(BaseTypes.int16Type(), ROOT).build();
        final var restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());
        assertEquals(Optional.empty(), restrictions.getLengthConstraint());
        assertEquals(List.of(), restrictions.getPatternConstraints());
    }

    @Test
    void getDefaultUnsignedIntegerRestrictionsTest() {
        final var type = DerivedTypes.derivedTypeBuilder(BaseTypes.uint16Type(), ROOT).build();
        final var restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertTrue(restrictions.isEmpty());
        assertEquals(Optional.empty(), restrictions.getLengthConstraint());
        assertEquals(List.of(), restrictions.getPatternConstraints());
    }

    @Test
    void getDefaultDecimalRestrictionsTest() {
        final var base = BaseTypes.decimalTypeBuilder(ROOT).setFractionDigits(10).build();
        final var type = DerivedTypes.derivedTypeBuilder(base, ROOT).build();

        final var restrictions = BindingGeneratorUtil.getRestrictions(type);

        assertNotNull(restrictions);
        assertFalse(restrictions.isEmpty());
        assertEquals(base.getRangeConstraint(), restrictions.getRangeConstraint());
        assertEquals(Optional.empty(), restrictions.getLengthConstraint());
        assertEquals(List.of(), restrictions.getPatternConstraints());
    }

    @Test
    void unicodeCharReplaceTest() {
        final var inputString = "abcu\\uuuuu\\uuua\\u\\\\uabc\\\\uuuu\\\\\\uuuu\\\\\\\\uuuu///uu/u/u/u/u/u/u";

        assertEquals("abcu\\\\uuuuu\\\\uuua\\\\u\\\\uabc\\\\uuuu\\\\uuuu\\\\uuuu///uu/u/u/u/u/u/u",
            BindingGeneratorUtil.replaceAllIllegalChars(inputString));
    }
}
