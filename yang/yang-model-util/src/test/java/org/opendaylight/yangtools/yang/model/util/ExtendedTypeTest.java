/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.google.common.base.Optional;
import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

public class ExtendedTypeTest {
    @Test
    public void canCreateExtendedType() {
        String namespace = "TestType";
        String revision = "2014-08-26";
        String localName = "testType";
        QName testType = QName.create(namespace, revision, localName);
        IntegerTypeDefinition int32 = BaseTypes.int32Type();
        String description = "This type is used for testing purpose";
        Optional<String> desc = Optional.of(description);
        String reference = "Test Reference";
        Optional<String> ref = Optional.of(reference);

        ExtendedType.Builder extendedTypeBuilder = ExtendedType.builder(testType, int32, desc, ref, SchemaPath.ROOT);

        int defValue = 12;
        extendedTypeBuilder.defaultValue(defValue);

        extendedTypeBuilder.status(Status.OBSOLETE);
        extendedTypeBuilder.addedByUses(false);

        int digits = 2;
        extendedTypeBuilder.fractionDigits(digits);

        String units = "KiloTest";
        extendedTypeBuilder.units(units);

        Number min = 3;
        Number max = 15;
        LengthConstraint lengthCons = BaseConstraints.newLengthConstraint(min, max, desc, ref);
        extendedTypeBuilder.lengths(Collections.singletonList(lengthCons));

        ExtendedType extendedType = extendedTypeBuilder.build();

        assertEquals("BaseType is int32", int32, extendedType.getBaseType());
        assertEquals("Description", description, extendedType.getDescription());
        assertEquals("Reference", reference, extendedType.getReference());
        assertEquals("Path", SchemaPath.ROOT, extendedType.getPath());
        assertEquals("Default Value is 12", defValue, extendedType.getDefaultValue());
        assertEquals("Status is OBSOLETE", Status.OBSOLETE, extendedType.getStatus());
        assertFalse("AddedByUses", extendedType.isAddedByUses());
        assertTrue("should be 2", digits == extendedType.getFractionDigits());
        assertTrue("Should contain description", extendedType.toString().contains(description));
        assertEquals("Units", units, extendedType.getUnits());
        assertEquals("Length Constraints", Collections.singletonList(lengthCons), extendedType.getLengthConstraints());
        assertTrue("Should contain name of type", extendedType.getQName().toString().contains(localName));

        assertEquals("extendedType should equals to itself",extendedType, extendedType);
        assertFalse("extendedType shouldn't equal to null", extendedType.equals(null));
        assertTrue("Hash code of unionType should be equal to itself",
                extendedType.hashCode() == extendedType.hashCode());

    }

}