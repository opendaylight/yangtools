/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.common.base.Optional;
import org.junit.Test;

public class MustDefinitionImplTest {

    @Test
    // We're testing equals()
    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    public void test() {
        MustDefinitionImpl mdiA;
        MustDefinitionImpl mdiB;
        mdiA = MustDefinitionImpl.create("mustStrA", Optional.of("descriptionA"), Optional.of("referenceA"), Optional.of("errorAppTagA"), Optional.of("errorMessageA"));

        assertEquals("mdiA should equals to itsefl", mdiA, mdiA);
        assertNotEquals("mdiA shouldn't equal to null", mdiA, null);
        assertNotEquals("mdiA shouldn't equal to object of other type", mdiA, "str");

        // test of equals method

        Optional<String> description = Optional.of("description");
        Optional<String> reference = Optional.of("reference");
        Optional<String> errorAppTag = Optional.of("errorAppTag");
        Optional<String> errorMessage = Optional.of("errorMesage");
        // //confirmation of equality
        mdiA = MustDefinitionImpl.create("mustStr", description, reference, errorAppTag, errorMessage);
        mdiB = MustDefinitionImpl.create("mustStr", description, reference, errorAppTag, errorMessage);
        assertEquals("mdiA should equal to mdiB", mdiA, mdiB);

        // // mustStr
        mdiA = MustDefinitionImpl.create("mstr", description, reference, errorAppTag, errorMessage);
        mdiB = MustDefinitionImpl.create("mustStr", description, reference, errorAppTag, errorMessage);
        assertNotEquals("mdiA shouldn't equal to mdiB", mdiA, mdiB);

        mdiA = MustDefinitionImpl.create("mustStrA", description, reference, errorAppTag, errorMessage);
        mdiB = MustDefinitionImpl.create("mustStrB", description, reference, errorAppTag, errorMessage);
        assertNotEquals("mdiA shouldn't equal to mdiB", mdiA, mdiB);

        // //description
        mdiA = MustDefinitionImpl.create("mustStr", Optional.<String>absent(), reference, errorAppTag, errorMessage);
        mdiB = MustDefinitionImpl.create("mustStr", description, reference, errorAppTag, errorMessage);
        assertNotEquals("mdiA shouldn't equal to mdiB", mdiA, mdiB);

        mdiA = MustDefinitionImpl.create("mustStr", Optional.of("descriptionA"), reference, errorAppTag, errorMessage);
        mdiB = MustDefinitionImpl.create("mustStr", Optional.of("descriptionB"), reference, errorAppTag, errorMessage);
        assertNotEquals("mdiA shouldn't equal to mdiB", mdiA, mdiB);

        // //reference
        mdiA = MustDefinitionImpl.create("mustStr", description, Optional.<String>absent(), errorAppTag, errorMessage);
        mdiB = MustDefinitionImpl.create("mustStr", description, reference, errorAppTag, errorMessage);
        assertNotEquals("mdiA shouldn't equal to mdiB", mdiA, mdiB);

        mdiA = MustDefinitionImpl.create("mustStr", description, Optional.of("referenceA"), errorAppTag, errorMessage);
        mdiB = MustDefinitionImpl.create("mustStr", description, Optional.of("referenceB"), errorAppTag, errorMessage);
        assertNotEquals("mdiA shouldn't equal to mdiB", mdiA, mdiB);

        assertEquals(description.get(), mdiA.getDescription());
        assertEquals(errorAppTag.get(), mdiA.getErrorAppTag());
        assertEquals(errorMessage.get(), mdiA.getErrorMessage());
        assertEquals("referenceA", mdiA.getReference());
        assertEquals(null, mdiA.getXpath());
    }
}