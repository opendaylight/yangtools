/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import org.junit.Test;

public class PatternConstraintImplTest {

    @Test
    public void testMethodsOfPatternConstraintImpl() {
        final String regexExp = "\\D";
        final Optional<String> description = Optional.of("test description");
        final Optional<String> reference = Optional.of("RFC 6020");
        final PatternConstraintImpl patternConstraint = new PatternConstraintImpl(regexExp, description, reference);
        final String regexExp2 = "\\s";
        final Optional<String> description2 = Optional.of("test description2");
        final Optional<String> reference2 = Optional.of("RFC 6020 http://tools.ietf.org/html/rfc6020#page-23");
        final PatternConstraintImpl patternConstraint2 = new PatternConstraintImpl(regexExp2, description2, reference2);
        final PatternConstraintImpl patternConstraint3 = patternConstraint;
        final PatternConstraintImpl patternConstraint4 = new PatternConstraintImpl(regexExp, description2, reference);
        final PatternConstraintImpl patternConstraint5 = new PatternConstraintImpl(regexExp2, description2, reference2);

        assertNotNull("Object of PatternConstraintImpl shouldn't be null.", patternConstraint);
        assertEquals("Description should be 'test description'.", "test description",
                patternConstraint.getDescription());
        assertEquals("Error app tag shouldn't be null.", "invalid-regular-expression",
                patternConstraint.getErrorAppTag());
        assertNotNull(patternConstraint.getErrorMessage());
        assertEquals("Reference should be equals 'RFC 6020'.", "RFC 6020", patternConstraint.getReference());
        assertEquals("Regular expression should be equls '\\D'.", "\\D", patternConstraint.getRegularExpression());
        assertNotEquals("Hash codes shouldn't be equals.", patternConstraint.hashCode(), patternConstraint2.hashCode());
        assertFalse("String representation shouldn't be empty.", patternConstraint.toString().isEmpty());

        assertTrue("Objects should be equals.", patternConstraint.equals(patternConstraint3));
        assertFalse("Objects shouldn't be equals.", patternConstraint.equals(patternConstraint2));
        assertFalse("Objects shouldn't be equals.", patternConstraint4.equals(patternConstraint));
        assertFalse("Objects shouldn't be equals.", patternConstraint5.equals(patternConstraint));
        assertFalse("Objects shouldn't be equals.", patternConstraint.equals("test"));
        assertFalse("Objects shouldn't be equals.", patternConstraint.equals(null));
    }
}
