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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;

/**
 * Test methods of classes AbstractSignedInteger and AbstracUnsignedInteger
 */
public class AbstractIntegerTest {

    private final QName qname = QName.create("Test");
    private final QName qname2 = QName.create("Test2");
    private final String description = "test description";
    private final String description2 = "another test description";
    private final Number min = 0;
    private final Number min2 = 1;
    private final Number max = 10;
    private final String units = "";
    private final String units2 = "m";

    @Test
    public void testMethodsOfAbstractSignedInteger() {
        final AbstractSignedInteger signedInteger = (AbstractSignedInteger) createAbstractInteger(true, qname, description, min, max, units);
        final AbstractSignedInteger signedInteger2 = (AbstractSignedInteger) createAbstractInteger(true, qname, description, min, max, units);
        final AbstractSignedInteger signedInteger3 = signedInteger;
        final AbstractSignedInteger signedInteger4 = (AbstractSignedInteger) createAbstractInteger(true, qname2, description, min, max, units);
        final AbstractSignedInteger signedInteger5 = (AbstractSignedInteger) createAbstractInteger(true, null, description, min, max, units);
        final AbstractSignedInteger signedInteger6 = (AbstractSignedInteger) createAbstractInteger(true, qname, description2, min, max, units);
        final AbstractSignedInteger signedInteger7 = (AbstractSignedInteger) createAbstractInteger(true, qname, null, min, max, units);
        final AbstractSignedInteger signedInteger8 = (AbstractSignedInteger) createAbstractInteger(true, qname, description, min2, max, units);
        final AbstractSignedInteger signedInteger9 = (AbstractSignedInteger) createAbstractInteger(true, qname, description, min, min, units);
        final AbstractSignedInteger signedInteger10 = (AbstractSignedInteger) createAbstractInteger(true, qname, description, min, max, units2);
        final AbstractSignedInteger signedInteger11 = (AbstractSignedInteger) createAbstractInteger(true, qname, description, min, max, null);

        assertNotNull("Object of AbstractSignedInteger shouldn't be null.", signedInteger);
        assertNull("Base type shuld be null.", signedInteger.getBaseType());
        assertTrue("Units should be emtpy.", signedInteger.getUnits().isEmpty());
        assertEquals("Local name of QName should be 'Test'.", "Test", signedInteger.getQName().getLocalName());
        assertEquals("Path from root should be 'Test'.", "Test", signedInteger.getPath().getPathFromRoot().iterator().next().getLocalName());
        assertEquals("Description should be 'test description'.", "test description", signedInteger.getDescription());
        assertNotNull("Reference shouldn't be null.", signedInteger.getReference());
        assertEquals("Status should be Current.", Status.CURRENT, signedInteger.getStatus());
        assertFalse("Range constraint shouldn't be empty.", signedInteger.getRangeConstraints().isEmpty());
        assertTrue("Unknown schema nodes should be empty.", signedInteger.getUnknownSchemaNodes().isEmpty());
        assertFalse("String representation shouldn't be empty.", signedInteger.toString().isEmpty());

        assertTrue("Objects should be equals.", signedInteger.equals(signedInteger2));
        assertTrue("Objects should be equals.", signedInteger.equals(signedInteger3));
        assertFalse("Objects shouldn't be equals.", signedInteger.equals("string"));
        assertFalse("Objects shouldn't be equals.", signedInteger.equals(null));
        assertFalse("Objects shouldn't be equals.", signedInteger.equals(signedInteger4));
        assertFalse("Objects shouldn't be equals.", signedInteger5.equals(signedInteger));
        assertFalse("Objects shouldn't be equals.", signedInteger.equals(signedInteger6));
        assertFalse("Objects shouldn't be equals.", signedInteger7.equals(signedInteger));
        assertFalse("Objects shouldn't be equals.", signedInteger.equals(signedInteger8));
        assertFalse("Objects shouldn't be equals.", signedInteger.equals(signedInteger9));
        assertFalse("Objects shouldn't be equals.", signedInteger.equals(signedInteger10));
        assertFalse("Objects shouldn't be equals.", signedInteger11.equals(signedInteger));
    }

    @Test
    public void testMethodsOfAbstractUnsignedInteger() {
        final AbstractUnsignedInteger unsignedInteger = (AbstractUnsignedInteger) createAbstractInteger(false, qname, description, min, max, units);
        final AbstractUnsignedInteger unsignedInteger2 = (AbstractUnsignedInteger) createAbstractInteger(false, qname, description, min, max, units);
        final AbstractUnsignedInteger unsignedInteger3 = unsignedInteger;
        final AbstractUnsignedInteger unsignedInteger4 = (AbstractUnsignedInteger) createAbstractInteger(false, qname2, description, min, max, units);
        final AbstractUnsignedInteger unsignedInteger5 = (AbstractUnsignedInteger) createAbstractInteger(false, null, description, min, max, units);
        final AbstractUnsignedInteger unsignedInteger6 = (AbstractUnsignedInteger) createAbstractInteger(false, qname, description2, min, max, units);
        final AbstractUnsignedInteger unsignedInteger7 = (AbstractUnsignedInteger) createAbstractInteger(false, qname, null, min, max, units);
        final AbstractUnsignedInteger unsignedInteger9 = (AbstractUnsignedInteger) createAbstractInteger(false, qname, description, min, min, units);
        final AbstractUnsignedInteger unsignedInteger10 = (AbstractUnsignedInteger) createAbstractInteger(false, qname, description, min, max, units2);
        final AbstractUnsignedInteger unsignedInteger11 = (AbstractUnsignedInteger) createAbstractInteger(false, qname, description, min, max, null);

        assertNotNull("Object of AbstractUnsignedInteger shouldn't be null.", unsignedInteger);
        assertNull("Base type shuld be null.", unsignedInteger.getBaseType());
        assertTrue("Units should be emtpy.", unsignedInteger.getUnits().isEmpty());
        assertEquals("Local name of QName should be 'Test'.", "Test", unsignedInteger.getQName().getLocalName());
        assertEquals("Path from root should be 'Test'.", "Test", unsignedInteger.getPath().getPathFromRoot().iterator().next().getLocalName());
        assertEquals("Description should be 'test description'.", "test description", unsignedInteger.getDescription());
        assertNotNull("Reference shouldn't be null.", unsignedInteger.getReference());
        assertEquals("Status should be Current.", Status.CURRENT, unsignedInteger.getStatus());
        assertFalse("Range constraint shouldn't be empty.", unsignedInteger.getRangeConstraints().isEmpty());
        assertTrue("Unknown schema nodes should be empty.", unsignedInteger.getUnknownSchemaNodes().isEmpty());
        assertFalse("String representation shouldn't be empty.", unsignedInteger.toString().isEmpty());

        assertEquals("Hash codes should be equals.", unsignedInteger.hashCode(), unsignedInteger2.hashCode());

        assertTrue("Objects should be equals.", unsignedInteger.equals(unsignedInteger2));
        assertTrue("Objects should be equals.", unsignedInteger.equals(unsignedInteger3));
        assertFalse("Objects shouldn't be equals.", unsignedInteger.equals("string"));
        assertFalse("Objects shouldn't be equals.", unsignedInteger.equals(null));
        assertFalse("Objects shouldn't be equals.", unsignedInteger.equals(unsignedInteger4));
        assertFalse("Objects shouldn't be equals.", unsignedInteger5.equals(unsignedInteger));
        assertFalse("Objects shouldn't be equals.", unsignedInteger.equals(unsignedInteger6));
        assertFalse("Objects shouldn't be equals.", unsignedInteger7.equals(unsignedInteger));
        assertFalse("Objects shouldn't be equals.", unsignedInteger.equals(unsignedInteger9));
        assertFalse("Objects shouldn't be equals.", unsignedInteger.equals(unsignedInteger10));
        assertFalse("Objects shouldn't be equals.", unsignedInteger11.equals(unsignedInteger));
    }

    private Object createAbstractInteger(final boolean isSigned, final QName qname, final String description, final Number min,
            final Number max, final String units) {
        if (isSigned) {
            return new SignedInteger(qname, description, min, max, units);
        }
        return new UnsignedInteger(qname, description, max, units);
    }

    class UnsignedInteger extends AbstractUnsignedInteger {

        public UnsignedInteger(final QName name, final String description, final Number maxRange, final String units) {
            super(name, description, maxRange, units);
        }

        @Override
        public Object getDefaultValue() {
            return 0;
        }
    }

    class SignedInteger extends AbstractSignedInteger {

        protected SignedInteger(final QName name, final String description, final Number minRange, final Number maxRange, final String units) {
            super(name, description, minRange, maxRange, units);
        }

        @Override
        public Object getDefaultValue() {
            return 5;
        }
    }
}
