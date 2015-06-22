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
import java.net.URI;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AnyXmlEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AugmentEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeviationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FeatureEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.GroupingEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IdentityEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.InputEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafListEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.MustEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.OutputEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RefineEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RpcEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.SubmoduleEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UsesEffectiveStatementImpl;

public class CoverageTest2 {

    private static final QNameModule coverageModule = QNameModule.create(URI.create("coverage2-ns"),
            SimpleDateFormatUtil.DEFAULT_DATE_REV);

    private static final QName choice1QName = QName.create(coverageModule, "choice1");
    private static final QName feature1QName = QName.create(coverageModule, "feature1");
    private static final QName rpc1QName = QName.create(coverageModule, "rpc1");
    private static final QName notification1QName = QName.create(coverageModule, "not1");
    private static final QName ext1QName = QName.create(coverageModule, "ext1");
    private static final QName extension1QName = QName.create(coverageModule, "extension1");
    private static final QName extension2QName = QName.create(coverageModule, "extension2");
    private static final QName identity1QName = QName.create(coverageModule, "identity1");
    private static final QName grp1QName = QName.create(coverageModule, "grp1");
    private static final QName cont1QName = QName.create(coverageModule, "cont1");
    private static final QName leafList1QName = QName.create(coverageModule, "ll1");
    private static final QName axmlQName = QName.create(coverageModule, "axml");
    private static final QName rpcInputQName = QName.create(coverageModule, "input");
    private static final QName rpcOutputQName = QName.create(coverageModule, "output");
    private static final QName rpcInputLeafQName = QName.create(coverageModule, "rpc-input-leaf");
    private static final QName leaf1QName = QName.create(coverageModule, "leaf1");

    private static Module cov2Submod;
    private static Module moduleCoverage2 = null;

    @Before
    public void setUp() throws Exception {
        YangStatementSourceImpl testYang1 = new YangStatementSourceImpl("/coverage/coverage2.yang", false);
        YangStatementSourceImpl testYang2 = new YangStatementSourceImpl("/coverage/coverage2-submod.yang", false);
        YangStatementSourceImpl testYang3 = new YangStatementSourceImpl("/coverage/coverage2-import.yang", false);

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, testYang1, testYang2, testYang3);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Set<Module> modules = result.getModules();
        assertEquals(2, modules.size());

        for (final Module module : modules) {
            if (module.getName().equals("coverage2")) {
                moduleCoverage2 = module;
            }
        }

        assertNotNull(moduleCoverage2);
        final Set<Module> submodules = moduleCoverage2.getSubmodules();
        assertEquals(1, submodules.size());

        cov2Submod = submodules.iterator().next();
        assertEquals("coverage2-submod", cov2Submod.getName());
    }

    @Test
    public void testSubmodule() throws ParseException {

        assertEquals(coverageModule.getNamespace(), cov2Submod.getNamespace());
        assertEquals(SimpleDateFormatUtil.getRevisionFormat().parse("1998-02-03"), cov2Submod.getRevision());
        assertEquals(null, cov2Submod.getPrefix());
        assertEquals("1", cov2Submod.getYangVersion());
        assertEquals("submod2 org", cov2Submod.getOrganization());
        assertEquals("submod2 contact", cov2Submod.getContact());
        assertTrue(cov2Submod.toString().contains(cov2Submod.getName()));
        assertTrue(cov2Submod.toString().contains(cov2Submod.getRevision().toString()));
        assertNull(cov2Submod.getDataChildByName("123"));

        StmtContext<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> submodule2ctx = (StmtContext<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>>) ((SubmoduleEffectiveStatementImpl) cov2Submod)
                .getStatementContext();
        final Module submodule2Copy = new SubmoduleEffectiveStatementImpl(submodule2ctx);
        assertEquals(cov2Submod, submodule2Copy);

        final Collection<DataSchemaNode> childNodes = cov2Submod.getChildNodes();
        assertEquals(2, childNodes.size());

        ChoiceSchemaNode choice1Node = null;
        for (final DataSchemaNode childNode : childNodes) {
            if (childNode.getQName().equals(choice1QName)) {
                choice1Node = (ChoiceSchemaNode) childNode;
            }
        }
        assertNotNull(choice1Node);
        assertEquals(choice1Node, cov2Submod.getDataChildByName(choice1QName));
        assertEquals(choice1Node, cov2Submod.getDataChildByName("choice1"));

        final Set<ModuleImport> imports = cov2Submod.getImports();
        assertEquals(1, imports.size());
        assertEquals("coverage2-import", imports.iterator().next().getModuleName());
        assertTrue(imports.iterator().next().toString().contains("coverage2-import"));

        final Set<AugmentationSchema> augmentations = cov2Submod.getAugmentations();
        assertEquals(2, augmentations.size());
        AugmentationSchema augment1 = null;
        final SchemaPath choice1Path = SchemaPath.create(true, choice1QName);
        for (final AugmentationSchema augmentation : augmentations) {
            if (augmentation.getTargetPath().equals(choice1Path)) {
                augment1 = augmentation;
            }
        }
        assertNotNull(augment1);
        assertTrue(augment1.toString().contains(augment1.getTargetPath().toString()));
        assertTrue(augment1.toString().contains("when=null"));
        assertEquals(SchemaPath.create(true, choice1QName), augment1.getTargetPath());
        assertEquals(1, augment1.getUnknownSchemaNodes().size());
        assertEquals(coverageModule.getNamespace(), ((NamespaceRevisionAware) augment1).getNamespace());
        assertEquals(coverageModule.getRevision(), ((NamespaceRevisionAware) augment1).getRevision());
        assertEquals("DECLARATION", cov2Submod.getSource());

        final StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> augment1Ctx = (StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>>) ((AugmentEffectiveStatementImpl) augment1)
                .getStatementContext();
        final AugmentEffectiveStatementImpl augment1Copy = new AugmentEffectiveStatementImpl(augment1Ctx);
        assertEquals(augment1, augment1Copy);
        assertEquals(0, augment1Copy.compareTo((AugmentEffectiveStatementImpl) augment1));

        assertEquals(1, cov2Submod.getFeatures().size());
        final FeatureDefinition feature1Node = cov2Submod.getFeatures().iterator().next();
        assertEquals(feature1QName, feature1Node.getQName());
        assertEquals(1, feature1Node.getUnknownSchemaNodes().size());
        assertTrue(feature1Node.toString().contains(feature1Node.getQName().toString()));

        StmtContext<QName, FeatureStatement, ?> feature1Ctx = ((FeatureEffectiveStatementImpl) feature1Node)
                .getStatementContext();
        final FeatureDefinition feature1Copy = new FeatureEffectiveStatementImpl(feature1Ctx);
        assertEquals(feature1Node, feature1Copy);

        assertEquals(1, cov2Submod.getRpcs().size());
        final RpcDefinition rpc1Node = cov2Submod.getRpcs().iterator().next();
        assertEquals(rpc1QName, rpc1Node.getQName());
        assertEquals(SchemaPath.create(true, rpc1QName), rpc1Node.getPath());
        assertEquals(1, rpc1Node.getTypeDefinitions().size());
        assertEquals(1, rpc1Node.getUnknownSchemaNodes().size());
        assertEquals(1, rpc1Node.getGroupings().size());
        assertTrue(rpc1Node.toString().contains(rpc1QName.toString()));
        assertTrue(rpc1Node.toString().contains(rpc1Node.getPath().toString()));

        StmtContext<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> rpc1Ctx = (StmtContext<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>>) ((RpcEffectiveStatementImpl) rpc1Node)
                .getStatementContext();
        final RpcDefinition rpc1Copy = new RpcEffectiveStatementImpl(rpc1Ctx);
        assertEquals(rpc1Node, rpc1Copy);

        final ContainerSchemaNode rpc1Input = rpc1Node.getInput();
        assertNotNull(rpc1Input);
        assertEquals(rpc1Input.getPath(), SchemaPath.create(true, rpc1QName, rpcInputQName));
        assertTrue(rpc1Input.isAugmenting());
        assertTrue(rpc1Input.isAddedByUses());
        assertTrue(rpc1Input.isConfiguration());
        assertNotNull(rpc1Input.getConstraints());
        assertEquals(0, rpc1Input.getAvailableAugmentations().size());
        assertEquals(1, rpc1Input.getUnknownSchemaNodes().size());
        assertFalse(rpc1Input.isPresenceContainer());
        assertTrue(rpc1Input.toString().contains(rpcInputQName.getLocalName()));

        StmtContext<QName, InputStatement, EffectiveStatement<QName, InputStatement>> rpc1InputCtx = (StmtContext<QName, InputStatement, EffectiveStatement<QName, InputStatement>>) ((InputEffectiveStatementImpl) rpc1Input)
                .getStatementContext();
        final ContainerSchemaNode rpc1InputCopy = new InputEffectiveStatementImpl(rpc1InputCtx);
        assertEquals(rpc1Input, rpc1InputCopy);

        final ContainerSchemaNode rpc1Output = rpc1Node.getOutput();
        assertNotNull(rpc1Output);
        assertEquals(rpc1Output.getPath(), SchemaPath.create(true, rpc1QName, rpcOutputQName));
        assertTrue(rpc1Output.isAugmenting());
        assertTrue(rpc1Output.isAddedByUses());
        assertTrue(rpc1Output.isConfiguration());
        assertNotNull(rpc1Output.getConstraints());
        assertEquals(0, rpc1Output.getAvailableAugmentations().size());
        assertEquals(1, rpc1Output.getUnknownSchemaNodes().size());
        assertFalse(rpc1Output.isPresenceContainer());
        assertTrue(rpc1Output.toString().contains(rpcOutputQName.getLocalName()));

        StmtContext<QName, OutputStatement, EffectiveStatement<QName, OutputStatement>> rpc1OutputCtx = (StmtContext<QName, OutputStatement, EffectiveStatement<QName, OutputStatement>>) ((OutputEffectiveStatementImpl) rpc1Output)
                .getStatementContext();
        final ContainerSchemaNode rpc1OutputCopy = new OutputEffectiveStatementImpl(rpc1OutputCtx);
        assertEquals(rpc1Output, rpc1OutputCopy);

        LeafSchemaNode rpcInputLeafnode = (LeafSchemaNode) rpc1Input.getDataChildByName(rpcInputLeafQName);
        assertNotNull(rpcInputLeafnode);
        assertEquals("none", rpcInputLeafnode.getUnits());
        assertTrue(rpcInputLeafnode.toString().contains(rpcInputLeafQName.toString()));

        StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> rpcInputLeafCtx = (StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>>) ((LeafEffectiveStatementImpl) rpcInputLeafnode)
                .getStatementContext();
        final LeafSchemaNode rpcInputLeafCopy = new LeafEffectiveStatementImpl(rpcInputLeafCtx);
        assertEquals(rpcInputLeafnode, rpcInputLeafCopy);

        assertEquals(1, cov2Submod.getNotifications().size());
        assertEquals(notification1QName, cov2Submod.getNotifications().iterator().next().getQName());

        assertEquals(1, cov2Submod.getExtensionSchemaNodes().size());
        assertEquals(ext1QName, cov2Submod.getExtensionSchemaNodes().iterator().next().getQName());

        assertEquals(1, cov2Submod.getIdentities().size());
        final IdentitySchemaNode identity1Node = cov2Submod.getIdentities().iterator().next();
        assertEquals(identity1QName, identity1Node.getQName());
        assertTrue(identity1Node.toString().contains(identity1Node.getQName().toString()));
        assertEquals(SchemaPath.create(true, identity1QName), identity1Node.getPath());
        assertEquals(1, identity1Node.getUnknownSchemaNodes().size());

        StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> identity1Ctx = (StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>>) ((IdentityEffectiveStatementImpl) identity1Node)
                .getStatementContext();
        final IdentitySchemaNode identity1Copy = new IdentityEffectiveStatementImpl(identity1Ctx);
        assertEquals(identity1Node, identity1Copy);

        assertEquals(1, cov2Submod.getDeviations().size());
        final Deviation deviation1 = cov2Submod.getDeviations().iterator().next();
        assertEquals(choice1Path, deviation1.getTargetPath());
        assertEquals(1, deviation1.getUnknownSchemaNodes().size());
        assertEquals(extension1QName, deviation1.getUnknownSchemaNodes().iterator().next().getQName());
        assertTrue(deviation1.toString().contains(deviation1.getTargetPath().toString()));
        assertTrue(deviation1.toString().contains("deviate=ADD, reference=null"));

        StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> deviation1Ctx = ((DeviationEffectiveStatementImpl) deviation1)
                .getStatementContext();
        final Deviation deviation1Copy = new DeviationEffectiveStatementImpl(deviation1Ctx);
        assertEquals(deviation1, deviation1Copy);

        assertEquals(1, cov2Submod.getTypeDefinitions().size());
        assertEquals(Int32.class, cov2Submod.getTypeDefinitions().iterator().next().getBaseType().getClass());

        assertEquals(1, cov2Submod.getUnknownSchemaNodes().size());
        assertEquals(extension1QName, cov2Submod.getUnknownSchemaNodes().iterator().next().getQName());

        assertEquals(2, cov2Submod.getGroupings().size());
        GroupingDefinition grp1Node = null;
        for (final GroupingDefinition grouping : cov2Submod.getGroupings()) {
            if (grouping.getQName().equals(grp1QName)) {
                grp1Node = grouping;
            }
        }
        assertNotNull(grp1Node);
        assertEquals(grp1QName, grp1Node.getQName());
        assertEquals(1, cov2Submod.getUses().size());
        final SchemaPath grp1Path = cov2Submod.getUses().iterator().next().getGroupingPath();
        assertEquals(grp1Node.getPath(), grp1Path);
        assertTrue(grp1Node.toString().contains(grp1Node.getQName().toString()));

        UsesNode usesGrp1Node = null;
        for (final UsesNode usesNode : cov2Submod.getUses()) {
            if (usesNode.getGroupingPath().equals(grp1Node.getPath())) {
                usesGrp1Node = usesNode;
            }
        }
        assertNotNull(usesGrp1Node);
        assertTrue(usesGrp1Node.toString().contains(grp1Path.toString()));
        assertEquals(1, ((UsesEffectiveStatementImpl) usesGrp1Node).getUnknownSchemaNodes().size());
        assertFalse(usesGrp1Node.isAddedByUses());
        assertFalse(usesGrp1Node.isAugmenting());

        final StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesGrp1Ctx = (StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>>) ((UsesEffectiveStatementImpl) usesGrp1Node)
                .getStatementContext();
        final UsesEffectiveStatementImpl usesGrp1Copy = new UsesEffectiveStatementImpl(usesGrp1Ctx);
        assertEquals(usesGrp1Node, usesGrp1Copy);

        final StmtContext<SchemaNodeIdentifier, ?, ?> refineLeaf1Stmt = StmtContextUtils.findFirstDeclaredSubstatement(
                usesGrp1Ctx, RefineStatement.class);
        final SchemaNode refineLeaf1Node = (RefineEffectiveStatementImpl) refineLeaf1Stmt.buildEffective();
        assertEquals(leaf1QName, refineLeaf1Node.getQName());
        assertEquals(SchemaPath.create(Collections.<QName> emptyList(), false), refineLeaf1Node.getPath());
        assertEquals(1, refineLeaf1Node.getUnknownSchemaNodes().size());

        StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> grp1Ctx = (StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>>) ((GroupingEffectiveStatementImpl) grp1Node)
                .getStatementContext();
        final GroupingDefinition grp1Copy = new GroupingEffectiveStatementImpl(grp1Ctx);
        assertEquals(grp1Node, grp1Copy);
    }

    @Test
    public void test() throws ParseException {

        final ContainerSchemaNode cont1Node = (ContainerSchemaNode) moduleCoverage2.getDataChildByName(cont1QName);
        assertNotNull(cont1Node);
        assertEquals("container cont1", cont1Node.toString());
        final Set<MustDefinition> cont1Must = cont1Node.getConstraints().getMustConstraints();
        assertEquals(1, cont1Must.size());
        final MustDefinition cont1MustNode = cont1Must.iterator().next();
        assertEquals("cont1-condition != 'true'", cont1MustNode.getXpath().toString());

        StmtContext<RevisionAwareXPath, MustStatement, ?> cont1MustCtx = ((MustEffectiveStatementImpl) cont1MustNode)
                .getStatementContext();
        final MustDefinition cont1MustCopy = new MustEffectiveStatementImpl(cont1MustCtx);
        assertEquals(cont1MustNode, cont1MustCopy);

        final LeafListSchemaNode leafList1Node = (LeafListSchemaNode) cont1Node.getDataChildByName(leafList1QName);
        assertNotNull(leafList1Node);
        assertTrue(leafList1Node.isAugmenting());
        assertTrue(leafList1Node.isAddedByUses());
        assertTrue(leafList1Node.isUserOrdered());
        assertTrue(leafList1Node.isConfiguration());
        assertEquals(leafList1Node.getPath(), ((DerivableSchemaNode) leafList1Node).getOriginal().get().getPath());
        assertTrue(leafList1Node.toString().contains(leafList1QName.toString()));

        StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> leafList1ctx = (StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>>) ((LeafListEffectiveStatementImpl) leafList1Node)
                .getStatementContext();
        final LeafListSchemaNode leafList1Copy = new LeafListEffectiveStatementImpl(leafList1ctx);
        assertEquals(leafList1Node, leafList1Copy);

        assertEquals(1, leafList1Node.getUnknownSchemaNodes().size());
        assertEquals(extension2QName, leafList1Node.getUnknownSchemaNodes().iterator().next().getQName());

        final ConstraintDefinition leafList1Constraints = leafList1Node.getConstraints();
        assertNotNull(leafList1Constraints);
        assertTrue(leafList1Constraints.isMandatory());
        assertTrue(leafList1Constraints.getMinElements() == 0);
        assertTrue(leafList1Constraints.getMaxElements() == Integer.MAX_VALUE);
        assertTrue(leafList1Constraints.toString().contains("minElements=0, maxElements=2147483647"));
        assertEquals(-2118853297, leafList1Constraints.hashCode());

        final AnyXmlSchemaNode axmlNode = (AnyXmlSchemaNode) moduleCoverage2.getDataChildByName(axmlQName);
        StmtContext<QName, AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>> ctx = (StmtContext<QName, AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>>) ((AnyXmlEffectiveStatementImpl) axmlNode)
                .getStatementContext();
        AnyXmlSchemaNode axmlCopy = new AnyXmlEffectiveStatementImpl(ctx);
        assertEquals(axmlNode, axmlCopy);
        assertEquals(SchemaPath.create(true, axmlQName), axmlNode.getPath());
        assertTrue(axmlNode.toString().contains(axmlNode.getQName().toString()));
        assertTrue(axmlNode.toString().contains(axmlNode.getPath().toString()));
    }

    private static void addSources(final BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }
}