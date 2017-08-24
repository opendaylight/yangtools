/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Range;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class TypesResolutionTest {
    private Set<Module> testedModules;

    @Before
    public void init() throws Exception {
        final StatementStreamSource yangFile = sourceForResource("/types/custom-types-test@2012-4-4.yang");
        final StatementStreamSource yangFileDependency1 = sourceForResource("/ietf/iana-timezones@2012-07-09.yang");
        final StatementStreamSource yangFileDependency2 = sourceForResource("/ietf/ietf-inet-types@2010-09-24.yang");
        final StatementStreamSource yangFileDependency3 = sourceForResource("/ietf/ietf-yang-types@2010-09-24.yang");

        testedModules = TestUtils.parseYangSources(yangFile, yangFileDependency1, yangFileDependency2,
                yangFileDependency3).getModules();
        assertEquals(4, testedModules.size());
    }

    @Test
    public void testIPVersion() {
        Module tested = TestUtils.findModule(testedModules, "ietf-inet-types");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        assertEquals(14, typedefs.size());

        TypeDefinition<?> type = TestUtils.findTypedef(typedefs, "ip-version");
        assertTrue(type.getDescription().contains("This value represents the version of the IP protocol."));
        assertTrue(type.getReference().contains("RFC 2460: Internet Protocol, Version 6 (IPv6) Specification"));

        EnumTypeDefinition enumType = (EnumTypeDefinition) type.getBaseType();
        List<EnumPair> values = enumType.getValues();
        assertEquals(3, values.size());

        EnumPair value0 = values.get(0);
        assertEquals("unknown", value0.getName());
        assertEquals(0, value0.getValue());
        assertEquals("An unknown or unspecified version of the Internet protocol.", value0.getDescription());

        EnumPair value1 = values.get(1);
        assertEquals("ipv4", value1.getName());
        assertEquals(1, value1.getValue());
        assertEquals("The IPv4 protocol as defined in RFC 791.", value1.getDescription());

        EnumPair value2 = values.get(2);
        assertEquals("ipv6", value2.getName());
        assertEquals(2, value2.getValue());
        assertEquals("The IPv6 protocol as defined in RFC 2460.", value2.getDescription());
    }

    @Test
    public void testEnumeration() {
        Module tested = TestUtils.findModule(testedModules, "custom-types-test");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();

        TypeDefinition<?> type = TestUtils.findTypedef(typedefs, "ip-version");
        EnumTypeDefinition enumType = (EnumTypeDefinition) type.getBaseType();
        List<EnumPair> values = enumType.getValues();
        assertEquals(4, values.size());

        EnumPair value0 = values.get(0);
        assertEquals("unknown", value0.getName());
        assertEquals(0, value0.getValue());
        assertEquals("An unknown or unspecified version of the Internet protocol.", value0.getDescription());

        EnumPair value1 = values.get(1);
        assertEquals("ipv4", value1.getName());
        assertEquals(19, value1.getValue());
        assertEquals("The IPv4 protocol as defined in RFC 791.", value1.getDescription());

        EnumPair value2 = values.get(2);
        assertEquals("ipv6", value2.getName());
        assertEquals(7, value2.getValue());
        assertEquals("The IPv6 protocol as defined in RFC 2460.", value2.getDescription());

        EnumPair value3 = values.get(3);
        assertEquals("default", value3.getName());
        assertEquals(20, value3.getValue());
        assertEquals("default ip", value3.getDescription());
    }

    @Test
    public void testIpAddress() {
        Module tested = TestUtils.findModule(testedModules, "ietf-inet-types");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> type = TestUtils.findTypedef(typedefs, "ip-address");
        UnionTypeDefinition baseType = (UnionTypeDefinition) type.getBaseType();
        List<TypeDefinition<?>> unionTypes = baseType.getTypes();

        StringTypeDefinition ipv4 = (StringTypeDefinition) unionTypes.get(0);
        assertNotNull(ipv4.getBaseType());
        String expectedPattern = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}"
                + "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])" + "(%[\\p{N}\\p{L}]+)?$";
        assertEquals(expectedPattern, ipv4.getPatternConstraints().get(0).getRegularExpression());

        StringTypeDefinition ipv6 = (StringTypeDefinition) unionTypes.get(1);
        assertNotNull(ipv6.getBaseType());
        List<PatternConstraint> ipv6Patterns = ipv6.getPatternConstraints();
        expectedPattern = "^((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}"
                + "((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|" + "(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}"
                + "(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))" + "(%[\\p{N}\\p{L}]+)?$";
        assertEquals(expectedPattern, ipv6Patterns.get(0).getRegularExpression());

        expectedPattern = "^(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|" + "((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)"
                + "(%.+)?$";
        assertEquals(expectedPattern, ipv6Patterns.get(1).getRegularExpression());
    }

    @Test
    public void testDomainName() {
        Module tested = TestUtils.findModule(testedModules, "ietf-inet-types");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        StringTypeDefinition type = (StringTypeDefinition) TestUtils.findTypedef(typedefs, "domain-name");
        assertNotNull(type.getBaseType());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertEquals(1, patterns.size());
        String expectedPattern = "^((([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.)*"
                + "([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.?)" + "|\\.$";
        assertEquals(expectedPattern, patterns.get(0).getRegularExpression());

        Map<Range<Integer>, ConstraintMetaDefinition> lengths = type.getLengthConstraints().asMapOfRanges();
        assertEquals(1, lengths.size());
        Range<Integer> length = lengths.keySet().iterator().next();
        assertEquals(Integer.valueOf(1), length.lowerEndpoint());
        assertEquals(Integer.valueOf(253), length.upperEndpoint());
    }

    @Test
    public void testInstanceIdentifier1() {
        Module tested = TestUtils.findModule(testedModules, "custom-types-test");
        LeafSchemaNode leaf = (LeafSchemaNode) tested.getDataChildByName(
                QName.create(tested.getQNameModule(), "inst-id-leaf1"));
        InstanceIdentifierTypeDefinition leafType = (InstanceIdentifierTypeDefinition) leaf.getType();
        assertFalse(leafType.requireInstance());
        assertEquals(1, leaf.getUnknownSchemaNodes().size());
    }

    @Test
    public void testInstanceIdentifier2() {
        Module tested = TestUtils.findModule(testedModules, "custom-types-test");
        LeafSchemaNode leaf = (LeafSchemaNode) tested.getDataChildByName(
                QName.create(tested.getQNameModule(), "inst-id-leaf2"));
        InstanceIdentifierTypeDefinition leafType = (InstanceIdentifierTypeDefinition) leaf.getType();
        assertFalse(leafType.requireInstance());
    }

    @Test
    public void testIdentity() {
        Module tested = TestUtils.findModule(testedModules, "custom-types-test");
        Set<IdentitySchemaNode> identities = tested.getIdentities();
        assertEquals(5, identities.size());
        IdentitySchemaNode cryptoAlg = null;
        IdentitySchemaNode cryptoBase = null;
        IdentitySchemaNode cryptoId = null;
        for (IdentitySchemaNode id : identities) {
            if (id.getQName().getLocalName().equals("crypto-alg")) {
                cryptoAlg = id;
            } else if ("crypto-base".equals(id.getQName().getLocalName())) {
                cryptoBase = id;
            } else if ("crypto-id".equals(id.getQName().getLocalName())) {
                cryptoId = id;
            }
        }
        assertNotNull(cryptoAlg);
        IdentitySchemaNode baseIdentity = cryptoAlg.getBaseIdentity();
        assertEquals("crypto-base", baseIdentity.getQName().getLocalName());
        assertTrue(cryptoAlg.getDerivedIdentities().isEmpty());
        assertNull(baseIdentity.getBaseIdentity());

        assertNotNull(cryptoBase);
        assertNull(cryptoBase.getBaseIdentity());
        assertEquals(3, cryptoBase.getDerivedIdentities().size());

        assertNotNull(cryptoId);
        assertEquals(1, cryptoId.getUnknownSchemaNodes().size());
    }

    @Test
    public void testBitsType1() {
        Module tested = TestUtils.findModule(testedModules, "custom-types-test");
        LeafSchemaNode leaf = (LeafSchemaNode) tested.getDataChildByName(
                QName.create(tested.getQNameModule(), "mybits"));
        BitsTypeDefinition leafType = (BitsTypeDefinition) leaf.getType();
        List<Bit> bits = leafType.getBits();
        assertEquals(3, bits.size());

        Bit bit1 = bits.get(0);
        assertEquals("disable-nagle", bit1.getName());
        assertEquals(0L, bit1.getPosition());

        Bit bit2 = bits.get(1);
        assertEquals("auto-sense-speed", bit2.getName());
        assertEquals(1L, bit2.getPosition());

        Bit bit3 = bits.get(2);
        assertEquals("10-Mb-only", bit3.getName());
        assertEquals(2L, bit3.getPosition());
    }

    @Test
    public void testBitsType2() {
        Module tested = TestUtils.findModule(testedModules, "custom-types-test");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> testedType = TestUtils.findTypedef(typedefs, "access-operations-type");

        BitsTypeDefinition bitsType = (BitsTypeDefinition) testedType.getBaseType();
        List<Bit> bits = bitsType.getBits();
        assertEquals(5, bits.size());

        Bit bit0 = bits.get(0);
        assertEquals("create", bit0.getName());
        assertEquals(0L, bit0.getPosition());

        Bit bit1 = bits.get(1);
        assertEquals("delete", bit1.getName());
        assertEquals(365L, bit1.getPosition());

        Bit bit2 = bits.get(2);
        assertEquals("read", bit2.getName());
        assertEquals(500L, bit2.getPosition());

        Bit bit3 = bits.get(3);
        assertEquals("update", bit3.getName());
        assertEquals(501L, bit3.getPosition());

        Bit bit4 = bits.get(4);
        assertEquals("exec", bit4.getName());
        assertEquals(502L, bit4.getPosition());
    }

    @Test
    public void testIanaTimezones() {
        Module tested = TestUtils.findModule(testedModules, "iana-timezones");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> testedType = TestUtils.findTypedef(typedefs, "iana-timezone");

        String expectedDesc = "A timezone location as defined by the IANA timezone";
        assertTrue(testedType.getDescription().contains(expectedDesc));
        assertNull(testedType.getReference());
        assertEquals(Status.CURRENT, testedType.getStatus());

        QName testedTypeQName = testedType.getQName();
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:iana-timezones"), testedTypeQName.getNamespace());
        assertEquals(TestUtils.createDate("2012-07-09"), testedTypeQName.getRevision());
        assertEquals("iana-timezone", testedTypeQName.getLocalName());

        EnumTypeDefinition enumType = (EnumTypeDefinition) testedType.getBaseType();
        List<EnumPair> values = enumType.getValues();
        assertEquals(415, values.size()); // 0-414

        EnumPair enum168 = values.get(168);
        assertEquals("America/Danmarkshavn", enum168.getName());
        assertEquals(168, enum168.getValue());
        assertEquals("east coast, north of Scoresbysund", enum168.getDescription());

        EnumPair enum374 = values.get(374);
        assertEquals("America/Indiana/Winamac", enum374.getName());
        assertEquals(374, enum374.getValue());
        assertEquals("Eastern Time - Indiana - Pulaski County", enum374.getDescription());
    }

    @Test
    public void testObjectId128() {
        Module tested = TestUtils.findModule(testedModules, "ietf-yang-types");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        StringTypeDefinition testedType = (StringTypeDefinition) TestUtils.findTypedef(typedefs,
                "object-identifier-128");

        List<PatternConstraint> patterns = testedType.getPatternConstraints();
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.get(0);
        assertEquals("^\\d*(\\.\\d*){1,127}$", pattern.getRegularExpression());

        QName testedTypeQName = testedType.getQName();
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:ietf-yang-types"), testedTypeQName.getNamespace());
        assertEquals(TestUtils.createDate("2010-09-24"), testedTypeQName.getRevision());
        assertEquals("object-identifier-128", testedTypeQName.getLocalName());

        StringTypeDefinition testedTypeBase = testedType.getBaseType();
        patterns = testedTypeBase.getPatternConstraints();
        assertEquals(1, patterns.size());

        pattern = patterns.get(0);
        assertEquals("^(([0-1](\\.[1-3]?[0-9]))|(2\\.(0|([1-9]\\d*))))(\\.(0|([1-9]\\d*)))*$",
                pattern.getRegularExpression());

        QName testedTypeBaseQName = testedTypeBase.getQName();
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:ietf-yang-types"), testedTypeBaseQName.getNamespace());
        assertEquals(TestUtils.createDate("2010-09-24"), testedTypeBaseQName.getRevision());
        assertEquals("object-identifier", testedTypeBaseQName.getLocalName());
    }

    @Test
    public void testIdentityref() {
        Module tested = TestUtils.findModule(testedModules, "custom-types-test");
        Set<TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> testedType = TestUtils.findTypedef(typedefs, "service-type-ref");
        IdentityrefTypeDefinition baseType = (IdentityrefTypeDefinition) testedType.getBaseType();
        QName identity = baseType.getIdentity().getQName();
        assertEquals(URI.create("urn:custom.types.demo"), identity.getNamespace());
        assertEquals(TestUtils.createDate("2012-04-16"), identity.getRevision());
        assertEquals("service-type", identity.getLocalName());

        LeafSchemaNode type = (LeafSchemaNode) tested.getDataChildByName(QName.create(tested.getQNameModule(), "type"));
        assertNotNull(type);
    }

    @Test
    public void testUnionWithExt() throws ReactorException {

        final StatementStreamSource yangFile1 = sourceForResource("/types/union-with-ext/extdef.yang");
        final StatementStreamSource yangFile2 = sourceForResource("/types/union-with-ext/unionbug.yang");
        final StatementStreamSource yangFile3 = sourceForResource("/ietf/ietf-inet-types@2010-09-24.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile1, yangFile2, yangFile3);

        final SchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void testUnionWithBits() throws ReactorException {

        final StatementStreamSource yangFile = sourceForResource("/types/union-with-bits/union-bits-model.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile);

        final SchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void testUnionInList() {
        final StatementStreamSource yangFile = sourceForResource("/types/union-in-list/unioninlisttest.yang");

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile);

        try {
            final SchemaContext result = reactor.buildEffective();
            fail("effective build should fail due to union in list; this is not allowed");
        } catch (Exception e) {
            assertEquals(SomeModifiersUnresolvedException.class, e.getClass());
            assertTrue(e.getCause() instanceof SourceException);
            assertTrue(e.getCause().getMessage().startsWith("union is not a YANG statement or use of extension"));
        }
    }
}
