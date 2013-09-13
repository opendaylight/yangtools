package org.opendaylight.yangtools.yang.parser.util;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

public class RefineHolderTest {

    @Test
    public void test() {
        // hashCode method test
        RefineHolder rh = new RefineHolder("module", 2104, "name");
        RefineHolder rh1 = new RefineHolder("module", 2104, "name");
        assertEquals("rh should equals to itsefl", rh, rh);
        assertFalse("rh shouldn't equal to null", rh.equals(null));
        assertFalse("rh shouldn't equal to object of other type", rh.equals(new String("str")));

        assertEquals("rh1 should equals to rh", rh, rh1);

        RefineHolder rh2 = new RefineHolder("module", 2104, null);
        assertFalse("rh shouldn't equal to rh2", rh2.equals(rh1));
        rh2 = new RefineHolder("module", 2104, "name2");
        assertFalse("rh shouldn't equal to rh2", rh.equals(rh2));

        assertEquals("Wrong hash code", 1557537141, rh.hashCode());

        testConfigurationEqualsBranch(rh, rh1);
        testDefaultStrEqualsBranch(rh, rh1);
        testDescriptionEqualsBranch(rh, rh1);
        testMandatoryEqualsBranch(rh, rh1);
        testMaxElementsEqualsBranch(rh, rh1);
        testMinElementsEqualsBranch(rh, rh1);
        testMustEqualsBranch(rh, rh1);
        testPresenceEqualsBranch(rh, rh1);
        testReferenceEqualsBranch(rh, rh1);
        testAddUnknownNodeBuilderEqualsBranch(rh, rh1);
        testParentEqualsBranch(rh, rh1);

    }

    private void testConfigurationEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setConfiguration(false);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setConfiguration(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setConfiguration(true);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setConfiguration(true);
        assertEquals("Wrong hash code", 1418571652, rh.hashCode());
    }

    private void testDefaultStrEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setDefaultStr("default string1");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setDefaultStr("default string1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setDefaultStr("default string");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setDefaultStr("default string");
        assertEquals("Wrong hash code", 799654580, rh.hashCode());
    }

    private void testDescriptionEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setDescription("description1");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setDescription("description1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setDescription("description");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setDescription("description");
        assertEquals("Wrong hash code", -1140225360, rh.hashCode());
    }

    private void testMandatoryEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMandatory(false);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMandatory(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMandatory(true);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMandatory(true);
        assertEquals("Wrong hash code", 1070616321, rh.hashCode());
    }

    private void testMaxElementsEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMaxElements(5400);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMaxElements(5400);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMaxElements(5435);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMaxElements(5435);
        assertEquals("Wrong hash code", 1404849148, rh.hashCode());
    }

    private void testMinElementsEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMinElements(16);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMinElements(16);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMinElements(159);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMinElements(159);
        assertEquals("Wrong hash code", 1661916861, rh.hashCode());
    }

    private void testMustEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setMust(new MustDefinitionImpl("mustStr1", "description1", "reference1", "errorAppTag1", "errorMessage1"));
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setMust(new MustDefinitionImpl("mustStr1", "description1", "reference1", "errorAppTag1", "errorMessage1"));
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setMust(new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage"));
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setMust(new MustDefinitionImpl("mustStr", "description", "reference", "errorAppTag", "errorMessage"));
        assertEquals("Wrong hash code", -1682252717, rh.hashCode());
    }

    private void testPresenceEqualsBranch(RefineHolder rh, RefineHolder rh1) {

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setPresence(false);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setPresence(false);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setPresence(true);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setPresence(true);
        assertEquals("Wrong hash code", -1682214556, rh.hashCode());
    }

    private void testReferenceEqualsBranch(RefineHolder rh, RefineHolder rh1) {

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setReference("reference1");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setReference("reference1");
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setReference("reference");
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setReference("reference");
        assertEquals("Wrong hash code", 1687597231, rh.hashCode());
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

    private void testAddUnknownNodeBuilderEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        URI simpleUri = null;
        simpleUri = getUri("very:simple:URI");
        assertNotNull("URI can't be null", simpleUri);

        UnknownSchemaNodeBuilder usnb = new UnknownSchemaNodeBuilder("usnb", 151, new QName(simpleUri, "tst"));
        UnknownSchemaNodeBuilder usnb1 = new UnknownSchemaNodeBuilder("usnb", 151, new QName(simpleUri, "tst"));

        URI uriA = getUri("some:uriA");
        assertNotNull("URI can't be null", simpleUri);
        QName qnameA = new QName(uriA, new Date(5000000), "some nameA");
        QName qnameB = new QName(uriA, new Date(6000000), "some nameB");
        List<QName> qnamesA = new ArrayList<>();
        List<QName> qnamesB = new ArrayList<>();
        qnamesA.add(qnameA);
        qnamesB.add(qnameB);
        SchemaPath schemaPathA = new SchemaPath(qnamesA, true);
        SchemaPath schemaPathB = new SchemaPath(qnamesB, true);

        usnb.setPath(schemaPathB);
        usnb1.setPath(schemaPathB);

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.addUnknownNodeBuilder(usnb);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.addUnknownNodeBuilder(usnb1);
        assertEquals("rh should equal to rh1", rh, rh1);
        usnb.setPath(schemaPathA);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        usnb1.setPath(schemaPathA);
        assertEquals("Wrong hash code", -266936297, rh.hashCode());

    }

    private void testParentEqualsBranch(RefineHolder rh, RefineHolder rh1) {
        URI simpleUri = null;
        simpleUri = getUri("very:simple:URI");
        assertNotNull("URI can't be null", simpleUri);

        UnknownSchemaNodeBuilder usnbA = new UnknownSchemaNodeBuilder("usnbA", 151, new QName(simpleUri, "tst"));
        UnknownSchemaNodeBuilder usnbB = new UnknownSchemaNodeBuilder("usnbB", 151, new QName(simpleUri, "tst"));
        UnknownSchemaNodeBuilder usnbAParent = new UnknownSchemaNodeBuilder("usnbAParent", 151, new QName(simpleUri,
                "tst"));
        usnbA.setParent(usnbAParent);

        assertEquals("rh should equal to rh1", rh, rh1);
        rh1.setParent(usnbB);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh.setParent(usnbB);
        assertEquals("rh should equal to rh1", rh, rh1);
        rh.setParent(usnbA);
        assertFalse("rh shouldn't equal to rh1", rh.equals(rh1));
        rh1.setParent(usnbA);
        assertEquals("Wrong hash code", -237383625, rh.hashCode());

        assertEquals("rh should equal to rh1", rh, rh1);
    }

}
