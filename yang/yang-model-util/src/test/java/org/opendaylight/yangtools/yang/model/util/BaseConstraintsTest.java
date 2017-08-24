/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

public class BaseConstraintsTest {

    @Test
    public void canCreateConstraints() {
        final Number min = 5;
        final Number max = 99;
        final String description = "Any description";
        final String reference = "any_ref";

        LengthConstraint lengthCons = BaseConstraints.newLengthConstraint(min, max, description, reference);

        assertEquals("LengthConstraints Get min", min, lengthCons.getMin());
        assertEquals("LengthConstraints Get max", max, lengthCons.getMax());
        assertEquals("LengthConstraints Get description", description, lengthCons.getDescription());
        assertEquals("LengthConstraints Get reference", reference, lengthCons.getReference());

        final String reg_exp = "x|z";
        final Optional<String> desc = Optional.of(description);
        final Optional<String> ref = Optional.of(reference);
        PatternConstraint patternCons = BaseConstraints.newPatternConstraint(reg_exp, desc, ref);

        assertEquals("PatternConstraints Get regex", reg_exp, patternCons.getRegularExpression());
        assertEquals("PatternConstraints Get description", description, patternCons.getDescription());
        assertEquals("PatternConstraints Get reference", reference, patternCons.getReference());
    }
}
