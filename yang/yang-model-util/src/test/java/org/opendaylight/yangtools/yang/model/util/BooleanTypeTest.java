package org.opendaylight.yangtools.yang.model.util;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;

import java.util.Collections;

import static org.junit.Assert.*;

public class BooleanTypeTest {

    @Test
    public void canCreateBooleanType() {
        BooleanType boolType = BooleanType.getInstance();
        String stringBoolType = boolType.toString();

        assertEquals("getPath gives List of BOOLEAN_QNAME",
                Collections.singletonList(BaseTypes.BOOLEAN_QNAME), boolType.getPath().getPathFromRoot());

        assertEquals("getQName gives BOOLEAN_QNAME", BaseTypes.BOOLEAN_QNAME, boolType.getQName());

        assertEquals("The boolean built-in type represents a boolean value.", boolType.getDescription());

        String strPath = boolType.getPath().toString();
        assertTrue("Should contain string of getPath", stringBoolType.contains(strPath));

        assertEquals("Should be empty string", "", boolType.getUnits());

        assertEquals("Base type is null", null, boolType.getBaseType());

        assertEquals("Default value is false", false, boolType.getDefaultValue());

        assertEquals("Status CURRENT", Status.CURRENT, boolType.getStatus());

        assertEquals("Should contain empty list", Collections.EMPTY_LIST, boolType.getUnknownSchemaNodes());
    }
}