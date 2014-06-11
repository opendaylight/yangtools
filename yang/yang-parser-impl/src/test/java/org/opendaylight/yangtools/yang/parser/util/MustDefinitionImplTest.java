/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.util.MustDefinitionImpl;

public class MustDefinitionImplTest {

    @Test
    public void test() {
        MustDefinitionImpl mdiA;
        MustDefinitionImpl mdiB;
        mdiA = new MustDefinitionImpl("mustStrA", "descriptionA", "referenceA", "errorAppTagA", "errorMessageA");

        assertEquals("mdiA should equals to itsefl", mdiA, mdiA);
        assertFalse("mdiA shouldn't equal to null", mdiA.equals(null));
        assertFalse("mdiA shouldn't equal to object of other type", mdiA.equals(new String("str")));

        // test of equals method

        // //confirmation of equality
        mdiA = new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage");
        mdiB = new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage");
        assertEquals("mdiA should equal to mdiB", mdiA, mdiB);

        // // mustStr
        mdiA = new MustDefinitionImpl(null, "description", "reference", "errorAppTag", "errorMessage");
        mdiB = new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage");
        assertFalse("mdiA shouldn't equal to mdiB", mdiA.equals(mdiB));

        mdiA = new MustDefinitionImpl("mustStrA", "description", "reference", "errorAppTag", "errorMessage");
        mdiB = new MustDefinitionImpl("mustStrB", "description", "reference", "errorAppTag", "errorMessage");
        assertFalse("mdiA shouldn't equal to mdiB", mdiA.equals(mdiB));

        // //description
        mdiA = new MustDefinitionImpl("mustStr", null, "reference", "errorAppTag", "errorMessage");
        mdiB = new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage");
        assertFalse("mdiA shouldn't equal to mdiB", mdiA.equals(mdiB));

        mdiA = new MustDefinitionImpl("mustStr", "descriptionA", "reference", "errorAppTag", "errorMessage");
        mdiB = new MustDefinitionImpl("mustStr", "descriptionB", "reference", "errorAppTag", "errorMessage");
        assertFalse("mdiA shouldn't equal to mdiB", mdiA.equals(mdiB));

        // //reference
        mdiA = new MustDefinitionImpl("mustStr", "description", null, "errorAppTag", "errorMessage");
        mdiB = new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage");
        assertFalse("mdiA shouldn't equal to mdiB", mdiA.equals(mdiB));

        mdiA = new MustDefinitionImpl("mustStr", "description", "referenceA", "errorAppTag", "errorMessage");
        mdiB = new MustDefinitionImpl("mustStr", "description", "referenceB", "errorAppTag", "errorMessage");
        assertFalse("mdiA shouldn't equal to mdiB", mdiA.equals(mdiB));

    }

}
