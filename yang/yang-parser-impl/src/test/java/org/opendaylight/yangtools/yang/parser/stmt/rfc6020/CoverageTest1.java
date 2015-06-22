/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.common.base.Optional;
import java.net.URI;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.BitsSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.CaseEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.CaseShorthandImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveConstraintDefinitionImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ExtensionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ListEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ModuleEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.NotificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RevisionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitsSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumSpecificationEffectiveStatementImpl;

public class CoverageTest1 {

    private static final QNameModule coverageModule;

    static {
        QNameModule coverageModuleInit = null;
        try {
            coverageModuleInit = QNameModule.create(URI.create("coverage1-ns"), SimpleDateFormatUtil
                    .getRevisionFormat().parse("1998-02-03"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        coverageModule = coverageModuleInit;
    }

    private static final String RFC6020 = "rfc6020";

    private static final QName testChoiceQName = QName.create(coverageModule, "test-choice");
    private static final QName case1QName = QName.create(testChoiceQName, "case1");
    private static final QName case4QName = QName.create(testChoiceQName, "case4");
    private static final QName ext1QName = QName.create(testChoiceQName, "ext1");
    private static final QName extension1QName = QName.create(testChoiceQName, "extension1");
    private static final QName list1QName = QName.create(testChoiceQName, "list1");
    private static final QName notif1QName = QName.create(testChoiceQName, "notif1");
    private static final QName id1QName = QName.create(testChoiceQName, "id1");
    private static final QName leafEnumQName = QName.create(testChoiceQName, "leaf-enum");
    private static final QName enum1QName = QName.create(testChoiceQName, "enum1");
    private Module moduleCoverage1;
    private StmtContext<String, ModuleStatement, ?> module1Ctx;

    @Before
    public void setUp() throws Exception {
        YangStatementSourceImpl testYang = new YangStatementSourceImpl("/coverage/coverage1.yang", false);

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, testYang);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Set<Module> modules = result.getModules();
        assertEquals(1, modules.size());

        for (final Module module : modules) {
            if (module.getName().equals("coverage1")) {
                moduleCoverage1 = module;
            }
        }
        assertNotNull(moduleCoverage1);

        module1Ctx = ((ModuleEffectiveStatementImpl) moduleCoverage1).getStatementContext();
        assertNotNull(module1Ctx);
    }

    @Test
    public void test() {

        assertTrue(moduleCoverage1.toString().contains(moduleCoverage1.getName()));
        assertTrue(moduleCoverage1.toString().contains(moduleCoverage1.getRevision().toString()));
        assertEquals("DECLARATION", moduleCoverage1.getSource());
        assertNull(moduleCoverage1.getDataChildByName("123"));

        final StmtContext<Date, ?, ?> module1RevisionStmt = StmtContextUtils.findFirstDeclaredSubstatement(module1Ctx,
                RevisionStatement.class);
        final RevisionEffectiveStatementImpl module1RevisionNode = (RevisionEffectiveStatementImpl) module1RevisionStmt
                .buildEffective();
        assertNotNull(module1RevisionNode);
        assertEquals("initial revision", module1RevisionNode.getDescription());
        assertEquals("initial reference", module1RevisionNode.getReference());

        final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> module1CoverageCtx = (StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>>) ((ModuleEffectiveStatementImpl) moduleCoverage1)
                .getStatementContext();
        final Module module1Copy = new ModuleEffectiveStatementImpl(module1CoverageCtx);
        assertEquals(moduleCoverage1, module1Copy);

        ChoiceSchemaNode testChoiceNode = (ChoiceSchemaNode) moduleCoverage1.getDataChildByName(testChoiceQName);
        assertEquals(testChoiceQName, testChoiceNode.getQName());
        assertEquals("case2", testChoiceNode.getDefaultCase());
        assertEquals(true, testChoiceNode.isConfiguration());
        assertTrue(testChoiceNode.toString().contains(testChoiceNode.getQName().toString()));

        final List<UnknownSchemaNode> testChoiceUnknownSchemaNodes = testChoiceNode.getUnknownSchemaNodes();
        assertEquals(1, testChoiceUnknownSchemaNodes.size());
        final UnknownSchemaNode extension2Node = testChoiceUnknownSchemaNodes.iterator().next();
        assertEquals("extension2", extension2Node.getQName().getLocalName());

        final Set<ChoiceCaseNode> cases = testChoiceNode.getCases();
        assertEquals(4, cases.size());

        final ChoiceCaseNode case1Node = testChoiceNode.getCaseNodeByName(case1QName);

        assertNotNull(case1Node);
        assertEquals(false, case1Node.isConfiguration());
        assertTrue(case1Node.toString().contains(case1Node.getQName().toString()));

        StmtContext<QName, CaseStatement, EffectiveStatement<QName, CaseStatement>> case1Ctx = (StmtContext<QName, CaseStatement, EffectiveStatement<QName, CaseStatement>>) ((CaseEffectiveStatementImpl) case1Node)
                .getStatementContext();
        final ChoiceCaseNode case1Copy = new CaseEffectiveStatementImpl(case1Ctx);
        assertEquals(case1Node, case1Copy);

        ChoiceCaseNode case1Shorthand = new CaseShorthandImpl(case1Node);
        assertEquals(case1Node.getQName(), case1Shorthand.getQName());
        assertEquals(SchemaPath.create(true, testChoiceQName), case1Shorthand.getPath());
        assertEquals(case1Node.isAugmenting(), case1Shorthand.isAugmenting());
        assertEquals(case1Node.isAddedByUses(), case1Shorthand.isAddedByUses());
        assertEquals(case1Node.isConfiguration(), case1Shorthand.isConfiguration());
        assertEquals(case1Node.getConstraints(), case1Shorthand.getConstraints());
        assertEquals(case1Node.getUnknownSchemaNodes(), case1Shorthand.getUnknownSchemaNodes());
        assertEquals(case1Node.getDescription(), case1Shorthand.getDescription());
        assertEquals(case1Node.getReference(), case1Shorthand.getReference());
        assertEquals(case1Node.getStatus(), case1Shorthand.getStatus());
        assertEquals(Collections.<TypeDefinition> emptySet(), case1Shorthand.getTypeDefinitions());
        assertEquals(Collections.<GroupingDefinition> emptySet(), case1Shorthand.getGroupings());
        assertEquals(Collections.<UsesNode> emptySet(), case1Shorthand.getUses());
        assertEquals(Collections.<AugmentationSchema> emptySet(), case1Shorthand.getAvailableAugmentations());
        assertEquals(case1Node, case1Shorthand.getDataChildByName(case1Shorthand.getQName()));
        assertEquals(case1Node, case1Shorthand.getDataChildByName(case1Shorthand.getQName().getLocalName()));

        final List<UnknownSchemaNode> case1unknownSchemaNodes = case1Node.getUnknownSchemaNodes();
        assertEquals(1, case1unknownSchemaNodes.size());
        final UnknownSchemaNode extension1Node = case1unknownSchemaNodes.iterator().next();
        assertEquals("extension1", extension1Node.getQName().getLocalName());
        assertEquals("extension1 description", extension1Node.getDescription());
        assertEquals("extension1 reference", extension1Node.getReference());
        assertEquals(Status.CURRENT, extension1Node.getStatus());
        final SchemaPath extension1Path = SchemaPath.create(true, testChoiceQName, case1QName, extension1QName);
        assertEquals(extension1Path, extension1Node.getPath());

        final ExtensionDefinition ext1Node = extension1Node.getExtensionDefinition();
        assertEquals(SchemaPath.create(true, ext1QName), ext1Node.getPath());
        assertEquals(1, ext1Node.getUnknownSchemaNodes().size());
        assertEquals(1387096705, ext1Node.hashCode());
        assertTrue(ext1Node.toString().contains(ext1Node.getQName().toString()));
        assertTrue(ext1Node.toString().contains(ext1Node.getPath().toString()));
        assertTrue(ext1Node.toString().contains(ext1Node.getUnknownSchemaNodes().toString()));

        StmtContext<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> ext1Ctx = (StmtContext<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>>) ((ExtensionEffectiveStatementImpl) ext1Node)
                .getStatementContext();
        final ExtensionDefinition ext1Copy = new ExtensionEffectiveStatementImpl(ext1Ctx);
        assertEquals(ext1Node, ext1Copy);

        final ChoiceCaseNode case4Node = testChoiceNode.getCaseNodeByName(case4QName);
        final Optional<? extends SchemaNode> originalCase4Node = ((DerivableSchemaNode) case4Node).getOriginal();
        assertTrue(originalCase4Node.isPresent());
        assertEquals(SchemaPath.create(true, testChoiceQName, case4QName), originalCase4Node.get().getPath());

        final ConstraintDefinition case1Constraints = case1Node.getConstraints();
        assertNotNull(case1Constraints);
        assertTrue(case1Constraints.getMinElements() == 0);
        assertTrue(case1Constraints.getMaxElements() == Integer.MAX_VALUE);

        final ConstraintDefinition case1ConstraintsCopy = new EffectiveConstraintDefinitionImpl(
                (EffectiveStatementBase<?, ?>) case1Node);
        assertEquals(case1Constraints, case1ConstraintsCopy);

        ListSchemaNode list1Node = (ListSchemaNode) moduleCoverage1.getDataChildByName(list1QName);
        assertTrue(list1Node.toString().contains(list1QName.getLocalName()));

        StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> list1Ctx = (StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>>) ((ListEffectiveStatementImpl) list1Node)
                .getStatementContext();
        final ListSchemaNode list1Copy = new ListEffectiveStatementImpl(list1Ctx);
        assertEquals(list1Node, list1Copy);

        NotificationDefinition notif1Node = moduleCoverage1.getNotifications().iterator().next();
        assertEquals(0, notif1Node.getAvailableAugmentations().size());
        assertTrue(notif1Node.toString().contains(notif1QName.toString()));

        StmtContext<QName, NotificationStatement, EffectiveStatement<QName, NotificationStatement>> notif1Ctx = (StmtContext<QName, NotificationStatement, EffectiveStatement<QName, NotificationStatement>>) ((NotificationEffectiveStatementImpl) notif1Node)
                .getStatementContext();
        final NotificationDefinition notif1Copy = new NotificationEffectiveStatementImpl(notif1Ctx);
        assertEquals(notif1Node, notif1Copy);
    }

    @Test
    public void testTypes() {

        final QName unionQName = QName.create(coverageModule, TypeUtils.UNION);
        final QName typeTestQName = QName.create(coverageModule, "type-test");

        final StmtContext<QName, ?, ?> leafTypeTestStmt = StmtContextUtils.findFirstDeclaredSubstatement(module1Ctx,
                LeafStatement.class);
        final StmtContext<String, ?, ?> unionTypeStmt = StmtContextUtils.findFirstDeclaredSubstatement(
                leafTypeTestStmt, TypeStatement.class);

        final StmtContext<String, ?, ?> binaryStmt = findFirstTypeSubstatement(unionTypeStmt, TypeUtils.BINARY);
        final BinaryTypeDefinition binaryNode = (BinaryTypeDefinition) binaryStmt.buildEffective();
        assertEquals(1, binaryNode.getLengthConstraints().size());
        assertNull(binaryNode.getBaseType());
        assertEquals("", binaryNode.getUnits());
        assertEquals(Collections.emptyList(), binaryNode.getDefaultValue());
        assertEquals(SchemaPath.create(true, QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.BINARY)),
                binaryNode.getPath());
        assertEquals(0, binaryNode.getUnknownSchemaNodes().size());
        assertTrue(binaryNode.getDescription().contains(TypeUtils.BINARY));
        assertTrue(binaryNode.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, binaryNode.getStatus());

        final StmtContext<String, ?, ?> bitsStmt = findFirstTypeSubstatement(unionTypeStmt, TypeUtils.BITS);
        final BitsTypeDefinition bitsNode = (BitsTypeDefinition) bitsStmt.buildEffective();
        assertEquals(2, bitsNode.getBits().size());
        assertNull(bitsNode.getBaseType());
        assertEquals("", bitsNode.getUnits());
        assertEquals(2, ((List) bitsNode.getDefaultValue()).size());
        final QName bitsQName = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.BITS);
        assertEquals(bitsQName, bitsNode.getQName());
        assertEquals(SchemaPath.create(true, typeTestQName, unionQName, bitsQName), bitsNode.getPath());
        assertEquals(0, bitsNode.getUnknownSchemaNodes().size());
        assertTrue(bitsNode.getDescription().contains(TypeUtils.BITS));
        assertTrue(bitsNode.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, bitsNode.getStatus());
        assertTrue(bitsNode.toString().contains(bitsQName.toString()));
        assertEquals(-103827322, bitsNode.hashCode());

        StmtContext<String, TypeStatement.BitsSpecification, EffectiveStatement<String, TypeStatement.BitsSpecification>> bitsCtx = (StmtContext<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>>) ((BitsSpecificationEffectiveStatementImpl) bitsNode)
                .getStatementContext();
        final BitsTypeDefinition bitsCopy = new BitsSpecificationEffectiveStatementImpl(bitsCtx);
        assertEquals(bitsNode, bitsCopy);

        final QName bit1QName = QName.create(coverageModule, "bit1");
        Bit bit1Node = null;
        for (final Bit bit : bitsNode.getBits()) {
            if (bit.getQName().equals(bit1QName)) {
                bit1Node = bit;
            }
        }
        assertNotNull(bit1Node);
        assertEquals(1, bit1Node.getPosition().longValue());
        assertEquals(bit1QName.getLocalName(), bit1Node.getName());
        assertEquals(bit1QName, bit1Node.getQName());
        assertEquals(SchemaPath.create(true, typeTestQName, unionQName, QName.create(coverageModule, TypeUtils.BITS),
                bit1QName), bit1Node.getPath());
        assertEquals(1, bit1Node.getUnknownSchemaNodes().size());
        assertEquals("bit1 desc", bit1Node.getDescription());
        assertEquals("bit1 ref", bit1Node.getReference());
        assertEquals(Status.DEPRECATED, bit1Node.getStatus());

        StmtContext<QName, BitStatement, ?> bit1Ctx = ((BitEffectiveStatementImpl) bit1Node).getStatementContext();
        final Bit bit1Copy = new BitEffectiveStatementImpl(bit1Ctx);
        assertEquals(bit1Node, bit1Copy);

        final StmtContext<String, ?, ?> booleanStmt = findFirstTypeSubstatement(unionTypeStmt, TypeUtils.BOOLEAN);
        final BooleanTypeDefinition booleanNode = (BooleanTypeDefinition) booleanStmt.buildEffective();
        assertNull(booleanNode.getBaseType());
        assertEquals("", booleanNode.getUnits());
        assertFalse((Boolean) booleanNode.getDefaultValue());
        final QName booleanQName = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.BOOLEAN);
        assertEquals(booleanQName, booleanNode.getQName());
        assertEquals(SchemaPath.create(true, booleanQName), booleanNode.getPath());
        assertEquals(0, booleanNode.getUnknownSchemaNodes().size());
        assertTrue(booleanNode.getDescription().contains(TypeUtils.BOOLEAN));
        assertTrue(booleanNode.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, booleanNode.getStatus());
        assertTrue(booleanNode.toString().contains(booleanQName.toString()));

        final StmtContext<String, ?, ?> emptyStmt = findFirstTypeSubstatement(unionTypeStmt, TypeUtils.EMPTY);
        final EmptyTypeDefinition emptyNode = (EmptyTypeDefinition) emptyStmt.buildEffective();
        assertNull(emptyNode.getBaseType());
        assertNull(emptyNode.getUnits());
        assertNull(emptyNode.getDefaultValue());
        final QName emptyQName = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.EMPTY);
        assertEquals(emptyQName, emptyNode.getQName());
        assertEquals(SchemaPath.create(true, emptyQName), emptyNode.getPath());
        assertEquals(0, emptyNode.getUnknownSchemaNodes().size());
        assertTrue(emptyNode.getDescription().contains(TypeUtils.EMPTY));
        assertTrue(emptyNode.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, emptyNode.getStatus());
        assertTrue(emptyNode.toString().contains(emptyQName.toString()));

        IdentitySchemaNode id1Node = null;
        for (final IdentitySchemaNode identitySchemaNode : moduleCoverage1.getIdentities()) {
            if (identitySchemaNode.getQName().equals(id1QName)) {
                id1Node = identitySchemaNode;
            }
        }
        assertNotNull(id1Node);

        final StmtContext<String, ?, ?> identityRefStmt = findFirstTypeSubstatement(unionTypeStmt,
                TypeUtils.IDENTITY_REF);
        final IdentityrefTypeDefinition identityRefNode = (IdentityrefTypeDefinition) identityRefStmt.buildEffective();
        assertNull(identityRefNode.getBaseType());
        assertEquals("", identityRefNode.getUnits());
        assertEquals(id1Node, identityRefNode.getIdentity());
        assertEquals(id1Node, identityRefNode.getDefaultValue());
        final QName identityRefQName = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.IDENTITY_REF);
        assertEquals(identityRefQName, identityRefNode.getQName());
        assertEquals(SchemaPath.create(true, typeTestQName, unionQName, identityRefQName), identityRefNode.getPath());
        assertEquals(0, identityRefNode.getUnknownSchemaNodes().size());
        assertTrue(identityRefNode.getDescription().contains(TypeUtils.IDENTITY_REF));
        assertTrue(identityRefNode.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, identityRefNode.getStatus());
        assertTrue(identityRefNode.toString().contains(identityRefQName.getLocalName()));

        final LeafSchemaNode leafEnumNode = (LeafSchemaNode) moduleCoverage1.getDataChildByName(leafEnumQName);
        assertNotNull(leafEnumNode);
        final StmtContext<String, ?, ?> enumerationStmt = findFirstTypeSubstatement(
                ((LeafEffectiveStatementImpl) leafEnumNode).getStatementContext(), TypeUtils.ENUMERATION);
        final EnumTypeDefinition enumerationNode = (EnumTypeDefinition) enumerationStmt.buildEffective();
        assertNotNull(enumerationNode);
        assertEquals(2, enumerationNode.getValues().size());
        assertNull(enumerationNode.getBaseType());
        assertEquals("", enumerationNode.getUnits());
        final QName enumerationQName = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.ENUMERATION);
        assertEquals(enumerationQName, enumerationNode.getQName());
        assertEquals(SchemaPath.create(true, leafEnumQName, enumerationQName), enumerationNode.getPath());
        assertEquals(0, enumerationNode.getUnknownSchemaNodes().size());
        assertTrue(enumerationNode.getDescription().contains(TypeUtils.ENUMERATION));
        assertTrue(enumerationNode.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, enumerationNode.getStatus());
        assertTrue(enumerationNode.toString().contains(enumerationQName.toString()));
        assertTrue(enumerationNode.toString().contains(leafEnumQName.toString()));

        StmtContext<String, TypeStatement.EnumSpecification, EffectiveStatement<String, TypeStatement.EnumSpecification>> enumerationCtx = (StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>>) ((EnumSpecificationEffectiveStatementImpl) enumerationNode)
                .getStatementContext();
        final EnumTypeDefinition enumerationCopy = new EnumSpecificationEffectiveStatementImpl(enumerationCtx);
        assertEquals(enumerationNode, enumerationCopy);

        EnumPair enum1Node = null;
        for (final EnumPair enumPair : enumerationNode.getValues()) {
            if (enumPair.getQName().equals(enum1QName)) {
                enum1Node = enumPair;
            }
        }
        assertNotNull(enum1Node);
        assertEquals(enum1QName.getLocalName(), enum1Node.getName());
        assertEquals(enum1QName, enum1Node.getQName());
        assertEquals(9, enum1Node.getValue().intValue());
        final QName enumerationNodeQName = QName.create(coverageModule, TypeUtils.ENUMERATION);
        assertEquals(SchemaPath.create(true, leafEnumQName, enumerationNodeQName, enum1QName), enum1Node.getPath());
        assertEquals(0, enum1Node.getUnknownSchemaNodes().size());
        assertEquals("enum1 desc", enum1Node.getDescription());
        assertEquals("enum1 ref", enum1Node.getReference());
        assertEquals(Status.DEPRECATED, enum1Node.getStatus());

        final StmtContext<String, ?, ?> int8Stmt = findFirstTypeSubstatement(unionTypeStmt, TypeUtils.INT8);
        final IntegerTypeDefinition int8Node = (IntegerTypeDefinition) int8Stmt.buildEffective();
        assertNotNull(int8Node);
        assertNull(int8Node.getBaseType());
        assertEquals("", int8Node.getUnits());
        assertNull(int8Node.getDefaultValue());
        assertEquals(1, int8Node.getRangeConstraints().size());
        final QName int8QName = QName.create(coverageModule, TypeUtils.INT8);
        assertEquals(SchemaPath.create(true, typeTestQName, unionQName, int8QName), int8Node.getPath());
        assertEquals(0, int8Node.getUnknownSchemaNodes().size());
        assertTrue(int8Node.getDescription().contains(TypeUtils.INT8));
        assertTrue(int8Node.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, int8Node.getStatus());
        assertEquals(2001344889, int8Node.hashCode());
        assertTrue(int8Node.toString().contains(
                QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.INT8).toString()));

        final StmtContext<String, ?, ?> uInt8Stmt = findFirstTypeSubstatement(unionTypeStmt, TypeUtils.UINT8);
        final UnsignedIntegerTypeDefinition uInt8Node = (UnsignedIntegerTypeDefinition) uInt8Stmt.buildEffective();
        assertNotNull(uInt8Node);
        assertNull(uInt8Node.getBaseType());
        assertEquals("", uInt8Node.getUnits());
        assertNull(uInt8Node.getDefaultValue());
        assertEquals(1, uInt8Node.getRangeConstraints().size());
        final QName uInt8QName = QName.create(coverageModule, TypeUtils.UINT8);
        assertEquals(SchemaPath.create(true, typeTestQName, unionQName, uInt8QName), uInt8Node.getPath());
        assertEquals(0, uInt8Node.getUnknownSchemaNodes().size());
        assertTrue(uInt8Node.getDescription().contains(TypeUtils.UINT8));
        assertTrue(uInt8Node.getReference().contains(RFC6020));
        assertEquals(Status.CURRENT, uInt8Node.getStatus());
        assertEquals(367889436, uInt8Node.hashCode());
        assertTrue(uInt8Node.toString().contains(
                QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.UINT8).toString()));

        final StmtContext<String, ?, ?> decimal64Stmt = findFirstTypeSubstatement(unionTypeStmt, TypeUtils.DECIMAL64);
        final DecimalTypeDefinition decimal64Node = (DecimalTypeDefinition) decimal64Stmt.buildEffective();
        assertNotNull(decimal64Node);
//        assertEquals(1, decimal64Node.getRangeConstraints().size());
        assertEquals(9, decimal64Node.getFractionDigits().intValue());
    }

    private static void addSources(final BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

    public static final <AT> StmtContext<AT, ?, ?> findFirstTypeSubstatement(StmtContext<?, ?, ?> stmtContext,
            String type) {
        Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements = stmtContext.declaredSubstatements();
        for (StmtContext<?, ?, ?> subStmtContext : declaredSubstatements) {
            if (subStmtContext.rawStatementArgument().equals(type)) {
                return (StmtContext<AT, ?, ?>) subStmtContext;
            }
        }
        return null;
    }
}