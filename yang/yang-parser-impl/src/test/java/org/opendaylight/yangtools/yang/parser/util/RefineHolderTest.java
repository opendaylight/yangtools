/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.google.common.base.Optional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.MustDefinitionImpl;
import org.opendaylight.yangtools.yang.parser.builder.api.RefineBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RefineHolderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilderImpl;

/**
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public class RefineHolderTest {

    private RefineHolderImpl rh;
    private RefineHolderImpl rh1;

    @Before
    public void init() {
        rh = new RefineHolderImpl("module", 2104, "name");
        rh1 = new RefineHolderImpl("module", 2104, "name");
    }


    @Test
    public void testRefineEquality() {
        // hashCode method test
        assertEquals("rh should equals to itsefl", rh, rh);
        assertNotEquals("rh shouldn't equal to null", rh, null);
        assertNotEquals("rh shouldn't equal to object of other type", rh, new String("str"));

        assertEquals("rh1 should equals to rh", rh, rh1);

        RefineBuilder rh2 = new RefineHolderImpl("module", 2104, null);
        assertNotEquals("rh shouldn't equal to rh2", rh2, rh1);
        rh2 = new RefineHolderImpl("module", 2104, "name2");
        assertNotEquals("rh shouldn't equal to rh2", rh, rh2);

        assertEquals("Wrong hash code", 1557537141, rh.hashCode());
    }

    @Test
    public void testConfigurationEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setConfiguration(false);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setConfiguration(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setConfiguration(true);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setConfiguration(true);
    }

    @Test
    public void testDefaultStrEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setDefaultStr("default string1");
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setDefaultStr("default string1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setDefaultStr("default string");
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setDefaultStr("default string");
    }

    @Test
    public void testDescriptionEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setDescription("description1");
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setDescription("description1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setDescription("description");
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setDescription("description");
    }

    @Test
    public void testMandatoryEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMandatory(false);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setMandatory(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMandatory(true);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setMandatory(true);
    }

    @Test
    public void testMaxElementsEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMaxElements(5400);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setMaxElements(5400);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMaxElements(5435);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setMaxElements(5435);
    }

    @Test
    public void testMinElementsEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMinElements(16);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setMinElements(16);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMinElements(159);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setMinElements(159);
    }

    @Test
    public void testMustEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMust(MustDefinitionImpl.create("mustStr1", Optional.of("description1"), Optional.of("reference1"),
                Optional.of("errorAppTag1"), Optional.of("errorMessage1")));
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setMust(MustDefinitionImpl.create("mustStr1", Optional.of("description1"), Optional.of("reference1"),
                Optional.of("errorAppTag1"), Optional.of("errorMessage1")));
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMust(MustDefinitionImpl.create("mustStr", Optional.of("description"), Optional.of("reference"),
                Optional.of("errorAppTag"), Optional.of("errorMessage")));
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setMust(MustDefinitionImpl.create("mustStr", Optional.of("description"), Optional.of("reference"),
                Optional.of("errorAppTag"), Optional.of("errorMessage")));
    }

    @Test
    public void testPresenceEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setPresence(false);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setPresence(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setPresence(true);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setPresence(true);
    }

    @Test
    public void testReferenceEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setReference("reference1");
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setReference("reference1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setReference("reference");
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setReference("reference");
    }

    private static URI getUri(final String uri) {
        URI simpleUri = null;
        boolean instantionated = false;
        try {
            simpleUri = new URI(uri);
            instantionated = true;
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
            assertTrue("Uri instance wasn't created.", instantionated);
        }
        return simpleUri;
    }

    @Test
    public void testAddUnknownNodeBuilderEqualsBranch() {
        URI simpleUri = null;
        simpleUri = getUri("very:simple:URI");
        assertNotNull("URI can't be null", simpleUri);

        URI uriA = getUri("some:uriA");
        assertNotNull("URI can't be null", simpleUri);
        QName qnameA = QName.create(uriA, new Date(5000000), "some nameA");
        QName qnameB = QName.create(uriA, new Date(6000000), "some nameB");
        List<QName> qnamesA = new ArrayList<>();
        List<QName> qnamesB = new ArrayList<>();
        qnamesA.add(qnameA);
        qnamesB.add(qnameB);
        SchemaPath schemaPathB = SchemaPath.create(qnamesB, true);

        UnknownSchemaNodeBuilderImpl usnb = new UnknownSchemaNodeBuilderImpl("usnb", 151, new QName(simpleUri, "tst"), schemaPathB);
        UnknownSchemaNodeBuilderImpl usnb1 = new UnknownSchemaNodeBuilderImpl("usnb", 151, new QName(simpleUri, "tst"), schemaPathB);

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.addUnknownNodeBuilder(usnb);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.addUnknownNodeBuilder(usnb1);
        assertEquals("rh should equal to rh1", rh, rh1);
    }

    @Test
    public void testParentEqualsBranch() {
        URI simpleUriA = getUri("very:simple:URI:a");
        URI simpleUriB = getUri("very:simple:URI:b");

        SchemaPath path = org.mockito.Mockito.mock(SchemaPath.class);

        UnknownSchemaNodeBuilderImpl usnbA = new UnknownSchemaNodeBuilderImpl("usnbA", 151, new QName(simpleUriA, "tst"), path);
        UnknownSchemaNodeBuilderImpl usnbB = new UnknownSchemaNodeBuilderImpl("usnbB", 151, new QName(simpleUriB, "tst"), path);
        UnknownSchemaNodeBuilderImpl usnbAParent = new UnknownSchemaNodeBuilderImpl("usnbAParent", 151, new QName(simpleUriA,
                "tst"), path);
        usnbA.setParent(usnbAParent);

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setParent(usnbB);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh.setParent(usnbB);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setParent(usnbA);
        assertNotEquals("rh shouldn't equal to rh1", rh, rh1);
        rh1.setParent(usnbA);

        assertEquals("rh should equal to rh1", rh, rh1);
    }

}
