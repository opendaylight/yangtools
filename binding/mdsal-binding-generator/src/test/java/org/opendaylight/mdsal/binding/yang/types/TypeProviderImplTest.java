/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@Ignore
public class TypeProviderImplTest {
    @Test(expected = IllegalArgumentException.class)
    public void testLeafRefRelativeSelfReference() {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource(
            "/leafref/leafref-relative-invalid.yang");
        final Module moduleRelative = schemaContext.findModules(XMLNamespace.of("urn:xml:ns:yang:lrr"))
            .iterator().next();
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final QName listNode = QName.create(moduleRelative.getQNameModule(), "neighbor");
        final QName leafNode = QName.create(moduleRelative.getQNameModule(), "neighbor-id");
        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName(listNode))
                .getDataChildByName(leafNode);
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeafRefAbsoluteSelfReference() {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource(
            "/leafref/leafref-absolute-invalid.yang");
        final Module moduleRelative = schemaContext.findModules(XMLNamespace.of("urn:xml:ns:yang:lra"))
            .iterator().next();
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final QName listNode = QName.create(moduleRelative.getQNameModule(), "neighbor");
        final QName leafNode = QName.create(moduleRelative.getQNameModule(), "neighbor-id");
        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName(listNode))
                .getDataChildByName(leafNode);
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        Type leafrefResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType);
    }

    @Test
    public void testLeafRefRelativeAndAbsoluteValidReference() {
        final EffectiveModelContext schemaContext =
            YangParserTestUtils.parseYangResource("/leafref/leafref-valid.yang");
        final Module moduleValid = schemaContext.findModules(XMLNamespace.of("urn:xml:ns:yang:lrv")).iterator().next();
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final QName listNode = QName.create(moduleValid.getQNameModule(), "neighbor");
        final QName leaf1Node = QName.create(moduleValid.getQNameModule(), "neighbor-id");
        DataSchemaNode leafrefRel = ((ListSchemaNode) moduleValid.getDataChildByName(listNode))
                .getDataChildByName(leaf1Node);
        LeafSchemaNode leafRel = (LeafSchemaNode) leafrefRel;
        TypeDefinition<?> leafTypeRel = leafRel.getType();
        Type leafrefRelResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeRel, leafRel);
        assertNotNull(leafrefRelResolvedType);

        final QName leaf2Node = QName.create(moduleValid.getQNameModule(), "neighbor2-id");
        DataSchemaNode leafrefAbs = ((ListSchemaNode) moduleValid.getDataChildByName(listNode))
                .getDataChildByName(leaf2Node);
        LeafSchemaNode leafAbs = (LeafSchemaNode) leafrefAbs;
        TypeDefinition<?> leafTypeAbs = leafAbs.getType();
        Type leafrefAbsResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeAbs, leafAbs);
        assertNotNull(leafrefAbsResolvedType);
    }

    @Test
    public void testMethodsOfTypeProviderImpl() {
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(
            YangParserTestUtils.parseYangResource("/base-yang-types.yang"));

        final SchemaPath refTypePath = SchemaPath.create(true, QName.create("", "cont1"), QName.create("", "list1"));
        final CodegenGeneratedTypeBuilder refType = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType"));
        typeProvider.putReferencedType(refTypePath, refType);
        final StringTypeDefinition stringType = BaseTypes.stringType();

        // test getAdditionalTypes() method
        assertEquals(1, typeProvider.getAdditionalTypes().size());
    }
}
