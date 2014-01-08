package org.opendaylight.yangtools.yang.parser.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

public class RefineHolderTest {

    private RefineHolder rh;
    private RefineHolder rh1;

    @Before
    public void init() {
        rh = new RefineHolder("module", 2104, "name");
        rh1 = new RefineHolder("module", 2104, "name");
    }


    @Test
    public void testRefineEquality() {
        // hashCode method test
        assertEquals("rh should equals to itsefl", rh, rh);
        assertFalse("rh shouldn't equal to null", rh.equals(null));
        assertFalse("rh shouldn't equal to object of other type", rh.equals(new String("str")));

        assertEquals("rh1 should equals to rh", rh, rh1);

        RefineHolder rh2 = new RefineHolder("module", 2104, null);
        assertFalse("rh shouldn't equal to rh2", rh2.equals(rh1));
        rh2 = new RefineHolder("module", 2104, "name2");
        assertFalse("rh shouldn't equal to rh2", rh.equals(rh2));

        assertEquals("Wrong hash code", 1557537141, rh.hashCode());
    }

    @Test
    public void testConfigurationEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setConfiguration(false);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setConfiguration(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setConfiguration(true);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setConfiguration(true);
    }

    @Test
    public void testDefaultStrEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setDefaultStr("default string1");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setDefaultStr("default string1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setDefaultStr("default string");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setDefaultStr("default string");
    }

    @Test
    public void testDescriptionEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setDescription("description1");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setDescription("description1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setDescription("description");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setDescription("description");
    }

    @Test
    public void testMandatoryEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMandatory(false);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMandatory(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMandatory(true);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMandatory(true);
    }

    @Test
    public void testMaxElementsEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMaxElements(5400);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMaxElements(5400);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMaxElements(5435);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMaxElements(5435);
    }

    @Test
    public void testMinElementsEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMinElements(16);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMinElements(16);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMinElements(159);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMinElements(159);
    }

    @Test
    public void testMustEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMust(new MustDefinitionImpl("mustStr1", "description1", "reference1", "errorAppTag1", "errorMessage1"));
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMust(new MustDefinitionImpl("mustStr1", "description1", "reference1", "errorAppTag1", "errorMessage1"));
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMust(new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage"));
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMust(new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage"));
    }

    @Test
    public void testPresenceEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setPresence(false);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setPresence(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setPresence(true);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setPresence(true);
    }

    @Test
    public void testReferenceEqualsBranch() {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setReference("reference1");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setReference("reference1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setReference("reference");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setReference("reference");
    }

    private URI getUri(String uri) {
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
        QName qnameA = new QName(uriA, new Date(5000000), "some nameA");
        QName qnameB = new QName(uriA, new Date(6000000), "some nameB");
        List<QName> qnamesA = new ArrayList<>();
        List<QName> qnamesB = new ArrayList<>();
        qnamesA.add(qnameA);
        qnamesB.add(qnameB);
        SchemaPath schemaPathB = new SchemaPath(qnamesB, true);

        UnknownSchemaNodeBuilder usnb = new UnknownSchemaNodeBuilder("usnb", 151, new QName(simpleUri, "tst"), schemaPathB);
        UnknownSchemaNodeBuilder usnb1 = new UnknownSchemaNodeBuilder("usnb", 151, new QName(simpleUri, "tst"), schemaPathB);

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.addUnknownNodeBuilder(usnb);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.addUnknownNodeBuilder(usnb1);
        assertEquals("rh should equal to rh1", rh, rh1);
    }

    @Test
    public void testParentEqualsBranch() {
        URI simpleUriA = getUri("very:simple:URI:a");
        URI simpleUriB = getUri("very:simple:URI:b");

        SchemaPath path = org.mockito.Mockito.mock(SchemaPath.class);

        UnknownSchemaNodeBuilder usnbA = new UnknownSchemaNodeBuilder("usnbA", 151, new QName(simpleUriA, "tst"), path);
        UnknownSchemaNodeBuilder usnbB = new UnknownSchemaNodeBuilder("usnbB", 151, new QName(simpleUriB, "tst"), path);
        UnknownSchemaNodeBuilder usnbAParent = new UnknownSchemaNodeBuilder("usnbAParent", 151, new QName(simpleUriA,
                "tst"), path);
        usnbA.setParent(usnbAParent);

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setParent(usnbB);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setParent(usnbB);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setParent(usnbA);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setParent(usnbA);

        assertEquals("rh should equal to rh1", rh, rh1);
    }

}
