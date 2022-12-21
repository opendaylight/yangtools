/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

class TypesResolutionTest {
    private static SchemaContext CONTEXT;

    @BeforeAll
    static void beforeClass() throws Exception {
        CONTEXT = TestUtils.parseYangSource(
            "/types/custom-types-test@2012-04-04.yang",
            "/ietf/iana-timezones@2012-07-09.yang",
            "/ietf/ietf-inet-types@2010-09-24.yang",
            "/ietf/ietf-yang-types@2010-09-24.yang");
        assertEquals(4, CONTEXT.getModules().size());
    }

    @Test
    void testIPVersion() {
        Module tested = CONTEXT.findModules("ietf-inet-types").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        assertEquals(14, typedefs.size());

        TypeDefinition<?> type = TestUtils.findTypedef(typedefs, "ip-version");
        assertTrue(type.getDescription().get().contains("This value represents the version of the IP protocol."));
        assertTrue(type.getReference().get().contains("RFC 2460: Internet Protocol, Version 6 (IPv6) Specification"));

        EnumTypeDefinition enumType = (EnumTypeDefinition) type.getBaseType();
        List<EnumPair> values = enumType.getValues();
        assertEquals(3, values.size());

        EnumPair value0 = values.get(0);
        assertEquals("unknown", value0.getName());
        assertEquals(0, value0.getValue());
        assertEquals(Optional.of("An unknown or unspecified version of the Internet protocol."),
            value0.getDescription());

        EnumPair value1 = values.get(1);
        assertEquals("ipv4", value1.getName());
        assertEquals(1, value1.getValue());
        assertEquals(Optional.of("The IPv4 protocol as defined in RFC 791."), value1.getDescription());

        EnumPair value2 = values.get(2);
        assertEquals("ipv6", value2.getName());
        assertEquals(2, value2.getValue());
        assertEquals(Optional.of("The IPv6 protocol as defined in RFC 2460."), value2.getDescription());
    }

    @Test
    void testEnumeration() {
        Module tested = CONTEXT.findModules("custom-types-test").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();

        TypeDefinition<?> type = TestUtils.findTypedef(typedefs, "ip-version");
        EnumTypeDefinition enumType = (EnumTypeDefinition) type.getBaseType();
        List<EnumPair> values = enumType.getValues();
        assertEquals(4, values.size());

        EnumPair value0 = values.get(0);
        assertEquals("unknown", value0.getName());
        assertEquals(0, value0.getValue());
        assertEquals(Optional.of("An unknown or unspecified version of the Internet protocol."),
            value0.getDescription());

        EnumPair value1 = values.get(1);
        assertEquals("ipv4", value1.getName());
        assertEquals(19, value1.getValue());
        assertEquals(Optional.of("The IPv4 protocol as defined in RFC 791."), value1.getDescription());

        EnumPair value2 = values.get(2);
        assertEquals("ipv6", value2.getName());
        assertEquals(7, value2.getValue());
        assertEquals(Optional.of("The IPv6 protocol as defined in RFC 2460."), value2.getDescription());

        EnumPair value3 = values.get(3);
        assertEquals("default", value3.getName());
        assertEquals(20, value3.getValue());
        assertEquals(Optional.of("default ip"), value3.getDescription());
    }

    @Test
    void testIpAddress() {
        Module tested = CONTEXT.findModules("ietf-inet-types").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> type = TestUtils.findTypedef(typedefs, "ip-address");
        UnionTypeDefinition baseType = (UnionTypeDefinition) type.getBaseType();
        List<TypeDefinition<?>> unionTypes = baseType.getTypes();

        StringTypeDefinition ipv4 = (StringTypeDefinition) unionTypes.get(0);
        assertNotNull(ipv4.getBaseType());
        String expectedPattern = "^(?:(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}"
            + "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])" + "(%[\\p{N}\\p{L}]+)?)$";
        assertEquals(expectedPattern, ipv4.getPatternConstraints().get(0).getJavaPatternString());

        StringTypeDefinition ipv6 = (StringTypeDefinition) unionTypes.get(1);
        assertNotNull(ipv6.getBaseType());
        List<PatternConstraint> ipv6Patterns = ipv6.getPatternConstraints();
        expectedPattern = "^(?:((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}"
            + "((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|" + "(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}"
            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))" + "(%[\\p{N}\\p{L}]+)?)$";
        assertEquals(expectedPattern, ipv6Patterns.get(0).getJavaPatternString());

        expectedPattern = "^(?:(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|" + "((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)"
            + "(%.+)?)$";
        assertEquals(expectedPattern, ipv6Patterns.get(1).getJavaPatternString());
    }

    @Test
    void testDomainName() {
        Module tested = CONTEXT.findModules("ietf-inet-types").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        StringTypeDefinition type = (StringTypeDefinition) TestUtils.findTypedef(typedefs, "domain-name");
        assertNotNull(type.getBaseType());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertEquals(1, patterns.size());
        String expectedPattern = "^(?:((([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.)*"
            + "([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.?)" + "|\\.)$";
        assertEquals(expectedPattern, patterns.get(0).getJavaPatternString());

        LengthConstraint lengths = type.getLengthConstraint().get();
        assertEquals(1, lengths.getAllowedRanges().asRanges().size());
        Range<Integer> length = lengths.getAllowedRanges().span();
        assertEquals(Integer.valueOf(1), length.lowerEndpoint());
        assertEquals(Integer.valueOf(253), length.upperEndpoint());
    }

    @Test
    void testInstanceIdentifier1() {
        Module tested = CONTEXT.findModules("custom-types-test").iterator().next();
        LeafSchemaNode leaf = (LeafSchemaNode) tested.getDataChildByName(
            QName.create(tested.getQNameModule(), "inst-id-leaf1"));
        InstanceIdentifierTypeDefinition leafType = (InstanceIdentifierTypeDefinition) leaf.getType();
        assertFalse(leafType.requireInstance());
        assertEquals(1,
            leaf.asEffectiveStatement().getDeclared().declaredSubstatements(UnrecognizedStatement.class).size());
    }

    @Test
    void testInstanceIdentifier2() {
        Module tested = CONTEXT.findModules("custom-types-test").iterator().next();
        LeafSchemaNode leaf = (LeafSchemaNode) tested.getDataChildByName(
            QName.create(tested.getQNameModule(), "inst-id-leaf2"));
        InstanceIdentifierTypeDefinition leafType = (InstanceIdentifierTypeDefinition) leaf.getType();
        assertFalse(leafType.requireInstance());
    }

    @Test
    void testIdentity() {
        Module tested = CONTEXT.findModules("custom-types-test").iterator().next();
        Collection<? extends IdentitySchemaNode> identities = tested.getIdentities();
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
        IdentitySchemaNode baseIdentity = Iterables.getOnlyElement(cryptoAlg.getBaseIdentities());
        assertEquals("crypto-base", baseIdentity.getQName().getLocalName());
        assertEquals(0, CONTEXT.getDerivedIdentities(cryptoAlg).size());
        assertEquals(0, baseIdentity.getBaseIdentities().size());

        assertNotNull(cryptoBase);
        assertTrue(cryptoBase.getBaseIdentities().isEmpty());
        assertEquals(3, CONTEXT.getDerivedIdentities(cryptoBase).size());

        assertNotNull(cryptoId);
        assertEquals(1,
            cryptoId.asEffectiveStatement().getDeclared().declaredSubstatements(UnrecognizedStatement.class).size());
    }

    @Test
    void testBitsType1() {
        Module tested = CONTEXT.findModules("custom-types-test").iterator().next();
        LeafSchemaNode leaf = (LeafSchemaNode) tested.getDataChildByName(
            QName.create(tested.getQNameModule(), "mybits"));
        BitsTypeDefinition leafType = (BitsTypeDefinition) leaf.getType();
        Iterator<? extends Bit> bits = leafType.getBits().iterator();

        Bit bit1 = bits.next();
        assertEquals("disable-nagle", bit1.getName());
        assertEquals(Uint32.ZERO, bit1.getPosition());

        Bit bit2 = bits.next();
        assertEquals("auto-sense-speed", bit2.getName());
        assertEquals(Uint32.ONE, bit2.getPosition());

        Bit bit3 = bits.next();
        assertEquals("only-10-Mb", bit3.getName());
        assertEquals(Uint32.TWO, bit3.getPosition());

        assertFalse(bits.hasNext());
    }

    @Test
    void testBitsType2() {
        Module tested = CONTEXT.findModules("custom-types-test").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> testedType = TestUtils.findTypedef(typedefs, "access-operations-type");

        BitsTypeDefinition bitsType = (BitsTypeDefinition) testedType.getBaseType();
        Iterator<? extends Bit> bits = bitsType.getBits().iterator();

        Bit bit0 = bits.next();
        assertEquals("create", bit0.getName());
        assertEquals(Uint32.ZERO, bit0.getPosition());

        Bit bit1 = bits.next();
        assertEquals("delete", bit1.getName());
        assertEquals(Uint32.valueOf(365), bit1.getPosition());

        Bit bit2 = bits.next();
        assertEquals("read", bit2.getName());
        assertEquals(Uint32.valueOf(500), bit2.getPosition());

        Bit bit3 = bits.next();
        assertEquals("update", bit3.getName());
        assertEquals(Uint32.valueOf(501), bit3.getPosition());

        Bit bit4 = bits.next();
        assertEquals("exec", bit4.getName());
        assertEquals(Uint32.valueOf(502), bit4.getPosition());

        assertFalse(bits.hasNext());
    }

    @Test
    void testIanaTimezones() {
        Module tested = CONTEXT.findModules("iana-timezones").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> testedType = TestUtils.findTypedef(typedefs, "iana-timezone");

        String expectedDesc = "A timezone location as defined by the IANA timezone";
        assertTrue(testedType.getDescription().get().contains(expectedDesc));
        assertFalse(testedType.getReference().isPresent());
        assertEquals(Status.CURRENT, testedType.getStatus());

        QName testedTypeQName = testedType.getQName();
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:iana-timezones"), testedTypeQName.getNamespace());
        assertEquals(Revision.ofNullable("2012-07-09"), testedTypeQName.getRevision());
        assertEquals("iana-timezone", testedTypeQName.getLocalName());

        EnumTypeDefinition enumType = (EnumTypeDefinition) testedType.getBaseType();
        List<EnumPair> values = enumType.getValues();
        // 0-414
        assertEquals(415, values.size());

        EnumPair enum168 = values.get(168);
        assertEquals("America/Danmarkshavn", enum168.getName());
        assertEquals(168, enum168.getValue());
        assertEquals(Optional.of("east coast, north of Scoresbysund"), enum168.getDescription());

        EnumPair enum374 = values.get(374);
        assertEquals("America/Indiana/Winamac", enum374.getName());
        assertEquals(374, enum374.getValue());
        assertEquals(Optional.of("Eastern Time - Indiana - Pulaski County"), enum374.getDescription());
    }

    @Test
    void testObjectId128() {
        Module tested = CONTEXT.findModules("ietf-yang-types").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        StringTypeDefinition testedType = (StringTypeDefinition) TestUtils.findTypedef(typedefs,
            "object-identifier-128");

        List<PatternConstraint> patterns = testedType.getPatternConstraints();
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.get(0);
        assertEquals("^(?:\\d*(\\.\\d*){1,127})$", pattern.getJavaPatternString());

        QName testedTypeQName = testedType.getQName();
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-types"), testedTypeQName.getNamespace());
        assertEquals(Revision.ofNullable("2010-09-24"), testedTypeQName.getRevision());
        assertEquals("object-identifier-128", testedTypeQName.getLocalName());

        StringTypeDefinition testedTypeBase = testedType.getBaseType();
        patterns = testedTypeBase.getPatternConstraints();
        assertEquals(1, patterns.size());

        pattern = patterns.get(0);
        assertEquals("^(?:(([0-1](\\.[1-3]?[0-9]))|(2\\.(0|([1-9]\\d*))))(\\.(0|([1-9]\\d*)))*)$",
            pattern.getJavaPatternString());

        QName testedTypeBaseQName = testedTypeBase.getQName();
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-types"),
            testedTypeBaseQName.getNamespace());
        assertEquals(Revision.ofNullable("2010-09-24"), testedTypeBaseQName.getRevision());
        assertEquals("object-identifier", testedTypeBaseQName.getLocalName());
    }

    @Test
    void testIdentityref() {
        Module tested = CONTEXT.findModules("custom-types-test").iterator().next();
        Collection<? extends TypeDefinition<?>> typedefs = tested.getTypeDefinitions();
        TypeDefinition<?> testedType = TestUtils.findTypedef(typedefs, "service-type-ref");
        IdentityrefTypeDefinition baseType = (IdentityrefTypeDefinition) testedType.getBaseType();
        QName identity = baseType.getIdentities().iterator().next().getQName();
        assertEquals(XMLNamespace.of("urn:custom.types.demo"), identity.getNamespace());
        assertEquals(Revision.ofNullable("2012-04-16"), identity.getRevision());
        assertEquals("service-type", identity.getLocalName());

        LeafSchemaNode type = (LeafSchemaNode) tested.getDataChildByName(QName.create(tested.getQNameModule(), "type"));
        assertNotNull(type);
    }

    @Test
    void testUnionWithExt() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/types/union-with-ext/extdef.yang"))
            .addSource(sourceForResource("/types/union-with-ext/unionbug.yang"))
            .addSource(sourceForResource("/ietf/ietf-inet-types@2010-09-24.yang"))
            .buildEffective();
        assertNotNull(result);
    }

    @Test
    void testUnionWithBits() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/types/union-with-bits/union-bits-model.yang"))
            .buildEffective();
        assertNotNull(result);
    }

    @Test
    void testUnionInList() {
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/types/union-in-list/unioninlisttest.yang"));

        try {
            final SchemaContext result = reactor.buildEffective();
            fail("effective build should fail due to union in list; this is not allowed");
        } catch (ReactorException e) {
            assertEquals(SomeModifiersUnresolvedException.class, e.getClass());
            assertTrue(e.getCause() instanceof SourceException);
            assertTrue(e.getCause().getMessage().startsWith("union is not a YANG statement or use of extension"));
        }
    }
}
