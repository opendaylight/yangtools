/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Iterables;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

class TypesResolutionTest extends AbstractYangTest {
    private static EffectiveModelContext CONTEXT;

    @BeforeAll
    static void beforeClass() {
        CONTEXT = assertEffectiveModel(
            "/types/custom-types-test@2012-04-04.yang",
            "/ietf/iana-timezones@2012-07-09.yang",
            "/ietf/ietf-inet-types@2010-09-24.yang",
            "/ietf/ietf-yang-types@2010-09-24.yang");
        assertEquals(4, CONTEXT.getModules().size());
    }

    @Test
    void testIPVersion() {
        var tested = CONTEXT.findModules("ietf-inet-types").iterator().next();
        var typedefs = tested.getTypeDefinitions();
        assertEquals(14, typedefs.size());

        var type = TestUtils.findTypedef(typedefs, "ip-version");
        assertThat(type.getDescription().orElseThrow())
            .contains("This value represents the version of the IP protocol.");
        assertThat(type.getReference().orElseThrow())
            .contains("RFC 2460: Internet Protocol, Version 6 (IPv6) Specification");

        var enumType = assertInstanceOf(EnumTypeDefinition.class, type.getBaseType());
        var values = enumType.getValues();
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
        var tested = CONTEXT.findModules("custom-types-test").iterator().next();
        var typedefs = tested.getTypeDefinitions();

        var type = TestUtils.findTypedef(typedefs, "ip-version");
        var enumType = assertInstanceOf(EnumTypeDefinition.class, type.getBaseType());
        var values = enumType.getValues();
        assertEquals(4, values.size());

        var value0 = values.get(0);
        assertEquals("unknown", value0.getName());
        assertEquals(0, value0.getValue());
        assertEquals(Optional.of("An unknown or unspecified version of the Internet protocol."),
            value0.getDescription());

        var value1 = values.get(1);
        assertEquals("ipv4", value1.getName());
        assertEquals(19, value1.getValue());
        assertEquals(Optional.of("The IPv4 protocol as defined in RFC 791."), value1.getDescription());

        var value2 = values.get(2);
        assertEquals("ipv6", value2.getName());
        assertEquals(7, value2.getValue());
        assertEquals(Optional.of("The IPv6 protocol as defined in RFC 2460."), value2.getDescription());

        var value3 = values.get(3);
        assertEquals("default", value3.getName());
        assertEquals(20, value3.getValue());
        assertEquals(Optional.of("default ip"), value3.getDescription());
    }

    @Test
    void testIpAddress() {
        var tested = CONTEXT.findModules("ietf-inet-types").iterator().next();
        var typedefs = tested.getTypeDefinitions();
        var type = TestUtils.findTypedef(typedefs, "ip-address");
        var baseType = assertInstanceOf(UnionTypeDefinition.class, type.getBaseType());
        var unionTypes = baseType.getTypes();

        var ipv4 = assertInstanceOf(StringTypeDefinition.class, unionTypes.get(0));
        assertNotNull(ipv4.getBaseType());
        assertEquals("""
            ^(?:(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}\
            ([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\
            (%[\\p{N}\\p{L}]+)?)$""", ipv4.getPatternConstraints().get(0).getJavaPatternString());

        var ipv6 = assertInstanceOf(StringTypeDefinition.class, unionTypes.get(1));
        assertNotNull(ipv6.getBaseType());
        var ipv6Patterns = ipv6.getPatternConstraints();
        assertEquals("""
            ^(?:((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}\
            ((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|\
            (((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}\
            (25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))\
            (%[\\p{N}\\p{L}]+)?)$""", ipv6Patterns.get(0).getJavaPatternString());
        assertEquals("""
            ^(?:(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|\
            ((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)\
            (%.+)?)$""", ipv6Patterns.get(1).getJavaPatternString());
    }

    @Test
    void testDomainName() {
        var tested = CONTEXT.findModules("ietf-inet-types").iterator().next();
        var typedefs = tested.getTypeDefinitions();
        var type = assertInstanceOf(StringTypeDefinition.class, TestUtils.findTypedef(typedefs, "domain-name"));
        assertNotNull(type.getBaseType());
        var patterns = type.getPatternConstraints();
        assertEquals(1, patterns.size());
        assertEquals("""
            ^(?:((([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.)*\
            ([a-zA-Z0-9_]([a-zA-Z0-9\\-_]){0,61})?[a-zA-Z0-9]\\.?)\
            |\\.)$""", patterns.get(0).getJavaPatternString());

        var lengths = type.getLengthConstraint().orElseThrow();
        assertEquals(1, lengths.getAllowedRanges().asRanges().size());
        var length = lengths.getAllowedRanges().span();
        assertEquals(Integer.valueOf(1), length.lowerEndpoint());
        assertEquals(Integer.valueOf(253), length.upperEndpoint());
    }

    @Test
    void testInstanceIdentifier1() {
        var tested = CONTEXT.findModules("custom-types-test").iterator().next();
        var leaf = assertInstanceOf(LeafSchemaNode.class,
            tested.getDataChildByName(QName.create(tested.getQNameModule(), "inst-id-leaf1")));
        var leafType = assertInstanceOf(InstanceIdentifierTypeDefinition.class, leaf.getType());
        assertFalse(leafType.requireInstance());
        assertEquals(1, leaf.asEffectiveStatement().requireDeclared().declaredSubstatements(UnrecognizedStatement.class)
            .size());
    }

    @Test
    void testInstanceIdentifier2() {
        var tested = CONTEXT.findModules("custom-types-test").iterator().next();
        var leaf = assertInstanceOf(LeafSchemaNode.class,
            tested.getDataChildByName(QName.create(tested.getQNameModule(), "inst-id-leaf2")));
        var leafType = assertInstanceOf(InstanceIdentifierTypeDefinition.class, leaf.getType());
        assertFalse(leafType.requireInstance());
    }

    @Test
    void testIdentity() {
        var tested = CONTEXT.findModules("custom-types-test").iterator().next();
        var identities = tested.getIdentities();
        assertEquals(5, identities.size());
        IdentitySchemaNode cryptoAlg = null;
        IdentitySchemaNode cryptoBase = null;
        IdentitySchemaNode cryptoId = null;
        for (var id : identities) {
            switch (id.getQName().getLocalName()) {
                case "crypto-alg" -> cryptoAlg = id;
                case "crypto-base" -> cryptoBase = id;
                case "crypto-id" -> cryptoId = id;
                default -> {
                    // no-op
                }
            }
        }
        assertNotNull(cryptoAlg);
        var baseIdentity = Iterables.getOnlyElement(cryptoAlg.getBaseIdentities());
        assertEquals("crypto-base", baseIdentity.getQName().getLocalName());
        assertEquals(0, CONTEXT.getDerivedIdentities(cryptoAlg).size());
        assertEquals(0, baseIdentity.getBaseIdentities().size());

        assertNotNull(cryptoBase);
        assertTrue(cryptoBase.getBaseIdentities().isEmpty());
        assertEquals(3, CONTEXT.getDerivedIdentities(cryptoBase).size());

        assertNotNull(cryptoId);
        assertEquals(1, cryptoId.asEffectiveStatement().requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class).size());
    }

    @Test
    void testBitsType1() {
        var tested = CONTEXT.findModules("custom-types-test").iterator().next();
        var leaf = assertInstanceOf(LeafSchemaNode.class,
            tested.getDataChildByName(QName.create(tested.getQNameModule(), "mybits")));
        var leafType = assertInstanceOf(BitsTypeDefinition.class, leaf.getType());
        var bits = leafType.getBits().iterator();

        var bit1 = bits.next();
        assertEquals("disable-nagle", bit1.getName());
        assertEquals(Uint32.ZERO, bit1.getPosition());

        var bit2 = bits.next();
        assertEquals("auto-sense-speed", bit2.getName());
        assertEquals(Uint32.ONE, bit2.getPosition());

        var bit3 = bits.next();
        assertEquals("only-10-Mb", bit3.getName());
        assertEquals(Uint32.TWO, bit3.getPosition());

        assertFalse(bits.hasNext());
    }

    @Test
    void testBitsType2() {
        var tested = CONTEXT.findModules("custom-types-test").iterator().next();
        var typedefs = tested.getTypeDefinitions();
        var testedType = TestUtils.findTypedef(typedefs, "access-operations-type");

        var bitsType = assertInstanceOf(BitsTypeDefinition.class, testedType.getBaseType());
        var bits = bitsType.getBits().iterator();

        var bit0 = bits.next();
        assertEquals("create", bit0.getName());
        assertEquals(Uint32.ZERO, bit0.getPosition());

        var bit1 = bits.next();
        assertEquals("delete", bit1.getName());
        assertEquals(Uint32.valueOf(365), bit1.getPosition());

        var bit2 = bits.next();
        assertEquals("read", bit2.getName());
        assertEquals(Uint32.valueOf(500), bit2.getPosition());

        var bit3 = bits.next();
        assertEquals("update", bit3.getName());
        assertEquals(Uint32.valueOf(501), bit3.getPosition());

        var bit4 = bits.next();
        assertEquals("exec", bit4.getName());
        assertEquals(Uint32.valueOf(502), bit4.getPosition());

        assertFalse(bits.hasNext());
    }

    @Test
    void testIanaTimezones() {
        var tested = CONTEXT.findModules("iana-timezones").iterator().next();
        var typedefs = tested.getTypeDefinitions();
        var testedType = TestUtils.findTypedef(typedefs, "iana-timezone");

        assertThat(testedType.getDescription().orElseThrow())
            .contains("A timezone location as defined by the IANA timezone");
        assertFalse(testedType.getReference().isPresent());
        assertEquals(Status.CURRENT, testedType.getStatus());

        QName testedTypeQName = testedType.getQName();
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:iana-timezones"), testedTypeQName.getNamespace());
        assertEquals(Revision.ofNullable("2012-07-09"), testedTypeQName.getRevision());
        assertEquals("iana-timezone", testedTypeQName.getLocalName());

        var enumType = assertInstanceOf(EnumTypeDefinition.class, testedType.getBaseType());
        var values = enumType.getValues();
        // 0-414
        assertEquals(415, values.size());

        var enum168 = values.get(168);
        assertEquals("America/Danmarkshavn", enum168.getName());
        assertEquals(168, enum168.getValue());
        assertEquals(Optional.of("east coast, north of Scoresbysund"), enum168.getDescription());

        var enum374 = values.get(374);
        assertEquals("America/Indiana/Winamac", enum374.getName());
        assertEquals(374, enum374.getValue());
        assertEquals(Optional.of("Eastern Time - Indiana - Pulaski County"), enum374.getDescription());
    }

    @Test
    void testObjectId128() {
        var tested = CONTEXT.findModules("ietf-yang-types").iterator().next();
        var typedefs = tested.getTypeDefinitions();
        var testedType = assertInstanceOf(StringTypeDefinition.class,
            TestUtils.findTypedef(typedefs, "object-identifier-128"));

        var patterns = testedType.getPatternConstraints();
        assertEquals(1, patterns.size());
        var pattern = patterns.get(0);
        assertEquals("^(?:\\d*(\\.\\d*){1,127})$", pattern.getJavaPatternString());

        QName testedTypeQName = testedType.getQName();
        assertEquals(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-types"), testedTypeQName.getNamespace());
        assertEquals(Revision.ofNullable("2010-09-24"), testedTypeQName.getRevision());
        assertEquals("object-identifier-128", testedTypeQName.getLocalName());

        var testedTypeBase = testedType.getBaseType();
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
        var tested = CONTEXT.findModules("custom-types-test").iterator().next();
        var typedefs = tested.getTypeDefinitions();
        var testedType = TestUtils.findTypedef(typedefs, "service-type-ref");
        var baseType = assertInstanceOf(IdentityrefTypeDefinition.class, testedType.getBaseType());
        QName identity = baseType.getIdentities().iterator().next().getQName();
        assertEquals(XMLNamespace.of("urn:custom.types.demo"), identity.getNamespace());
        assertEquals(Revision.ofNullable("2012-04-16"), identity.getRevision());
        assertEquals("service-type", identity.getLocalName());

        LeafSchemaNode type = (LeafSchemaNode) tested.getDataChildByName(QName.create(tested.getQNameModule(), "type"));
        assertNotNull(type);
    }

    @Test
    void testUnionWithExt() {
        assertEffectiveModel(
            "/types/union-with-ext/extdef.yang",
            "/types/union-with-ext/unionbug.yang",
            "/ietf/ietf-inet-types@2010-09-24.yang");
    }

    @Test
    void testUnionWithBits() {
        assertEffectiveModel("/types/union-with-bits/union-bits-model.yang");
    }

    @Test
    void testUnionInList() {
        assertSourceException(startsWith("union is not a YANG statement or use of extension"),
            "/types/union-in-list/unioninlisttest.yang");
    }
}
