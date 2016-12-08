/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.global;
import static org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.sourceLocal;
import static org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.treeScoped;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameCacheNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.AnyxmlSchemaLocationNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.AugmentToChoiceNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToSemVerModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleQNameToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StmtOrderingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.AnydataStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ContainerStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ModifierStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ModuleStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.PatternStatementRfc7950Support;

public final class YangInferencePipeline {
    public static final Set<YangVersion> SUPPORTED_VERSIONS =
            Sets.immutableEnumSet(YangVersion.VERSION_1, YangVersion.VERSION_1_1);

    public static final StatementSupportBundle INIT_BUNDLE = StatementSupportBundle
            .builder(SUPPORTED_VERSIONS).addSupport(global(ValidationBundlesNamespace.class))
            .addSupport(global(SupportedFeaturesNamespace.class))
            .build();

    public static final StatementSupportBundle PRE_LINKAGE_BUNDLE = StatementSupportBundle
            .derivedFrom(INIT_BUNDLE)
            .addVersionSpecificSupport(YangVersion.VERSION_1, new ModuleStatementSupport())
            .addVersionSpecificSupport(YangVersion.VERSION_1_1, new ModuleStatementRfc7950Support())
            .addSupport(new SubmoduleStatementImpl.Definition())
            .addSupport(new NamespaceStatementImpl.Definition())
            .addSupport(new ImportStatementDefinition())
            .addSupport(new IncludeStatementImpl.Definition())
            .addSupport(new PrefixStatementImpl.Definition())
            .addSupport(new YangVersionStatementImpl.Definition())
            .addSupport(new RevisionStatementImpl.Definition())
            .addSupport(global(ModuleNameToNamespace.class))
            .addSupport(global(PreLinkageModuleNamespace.class))
            .addSupport(sourceLocal(ImpPrefixToNamespace.class))
            .addSupport(global(ModuleCtxToModuleQName.class))
            .build();

    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle
            .derivedFrom(PRE_LINKAGE_BUNDLE)
            .addSupport(new DescriptionStatementImpl.Definition())
            .addSupport(new RevisionDateStatementImpl.Definition())
            .addSupport(new ReferenceStatementImpl.Definition())
            .addSupport(new ContactStatementImpl.Definition())
            .addSupport(new OrganizationStatementImpl.Definition())
            .addSupport(new BelongsToStatementImpl.Definition())
            .addSupport(global(ModuleNamespace.class))
            .addSupport(global(ModuleNamespaceForBelongsTo.class))
            .addSupport(global(SubmoduleNamespace.class))
            .addSupport(global(NamespaceToModule.class))
            .addSupport(global(ModuleNameToModuleQName.class))
            .addSupport(global(ModuleCtxToModuleIdentifier.class))
            .addSupport(global(ModuleQNameToModuleName.class))
            .addSupport(global(PrefixToModule.class))
            .addSupport(global(ModuleIdentifierToModuleQName.class))
            .addSupport(QNameCacheNamespace.getInstance())
            .addSupport(sourceLocal(ImportedModuleContext.class))
            .addSupport(sourceLocal(IncludedModuleContext.class))
            .addSupport(sourceLocal(IncludedSubmoduleNameToIdentifier.class))
            .addSupport(sourceLocal(ImpPrefixToModuleIdentifier.class))
            .addSupport(sourceLocal(BelongsToPrefixToModuleIdentifier.class))
            .addSupport(sourceLocal(URIStringToImpPrefix.class))
            .addSupport(sourceLocal(BelongsToModuleContext.class))
            .addSupport(sourceLocal(QNameToStatementDefinition.class))
            .addSupport(sourceLocal(BelongsToPrefixToModuleName.class))
            .addSupport(new SemanticVersionStatementImpl.SemanticVersionSupport())
            .addSupport(global(SemanticVersionNamespace.class))
            .addSupport(global(SemanticVersionModuleNamespace.class))
            .addSupport(sourceLocal(ImpPrefixToSemVerModuleIdentifier.class))
            .build();

    public static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle
            .derivedFrom(LINKAGE_BUNDLE)
            .addSupport(new YinElementStatementImpl.Definition())
            .addSupport(new ArgumentStatementImpl.Definition())
            .addSupport(new ExtensionStatementImpl.Definition())
            .addSupport(new ChildSchemaNodes<>())
            .addSupport(new SchemaNodeIdentifierBuildNamespace())
            .addSupport(global(ExtensionNamespace.class))
            .addSupport(new TypedefStatementImpl.Definition())
            .addSupport(treeScoped(TypeNamespace.class))
            .addSupport(new IdentityStatementImpl.Definition())
            .addSupport(global(IdentityNamespace.class))
            .addSupport(new DefaultStatementImpl.Definition())
            .addSupport(new StatusStatementImpl.Definition())
            .addSupport(new TypeStatementImpl.Definition())
            .addSupport(new UnitsStatementImpl.Definition())
            .addSupport(new RequireInstanceStatementImpl.Definition())
            .addSupport(new BitStatementImpl.Definition())
            .addSupport(new PathStatementImpl.Definition())
            .addSupport(new EnumStatementImpl.Definition())
            .addSupport(new LengthStatementImpl.Definition())
            .addVersionSpecificSupport(YANG1, new PatternStatementImpl.Definition())
            .addVersionSpecificSupport(YANG1_1, new PatternStatementRfc7950Support())
            .addVersionSpecificSupport(YANG1_1, new ModifierStatementImpl.Definition())
            .addSupport(new RangeStatementImpl.Definition())
            .addVersionSpecificSupport(YangVersion.VERSION_1, new ContainerStatementImpl.Definition())
            .addVersionSpecificSupport(YangVersion.VERSION_1_1, new ContainerStatementRfc7950Support())
            .addSupport(new GroupingStatementImpl.Definition())
            .addSupport(new ListStatementImpl.Definition())
            .addSupport(new UniqueStatementImpl.Definition())
            .addSupport(new RpcStatementImpl.Definition())
            .addSupport(new InputStatementImpl.Definition())
            .addSupport(new OutputStatementImpl.Definition())
            .addSupport(new NotificationStatementImpl.Definition())
            .addSupport(new FractionDigitsStatementImpl.Definition())
            .addSupport(new BaseStatementImpl.Definition())
            .addSupport(global(DerivedIdentitiesNamespace.class))
            .addSupport(global(StatementDefinitionNamespace.class))
            .build();

    public static final StatementSupportBundle FULL_DECL_BUNDLE = StatementSupportBundle
            .derivedFrom(STMT_DEF_BUNDLE)
            .addSupport(new LeafStatementImpl.Definition())
            .addSupport(new ConfigStatementImpl.Definition())
            .addSupport(new DeviationStatementImpl.Definition())
            .addSupport(new DeviateStatementImpl.Definition())
            .addSupport(new ChoiceStatementImpl.Definition())
            .addSupport(new CaseStatementImpl.Definition())
            .addSupport(new MustStatementImpl.Definition())
            .addSupport(new MandatoryStatementImpl.Definition())
            .addSupport(new AnyxmlStatementImpl.Definition())
            .addVersionSpecificSupport(YangVersion.VERSION_1_1, new AnydataStatementImpl.Definition())
            .addSupport(new IfFeatureStatementImpl.Definition())
            .addSupport(new UsesStatementImpl.Definition())
            .addSupport(treeScoped(GroupingNamespace.class)) //treeScoped
            .addSupport(new ErrorMessageStatementImpl.Definition())
            .addSupport(new ErrorAppTagStatementImpl.Definition())
            .addSupport(new LeafListStatementImpl.Definition())
            .addSupport(new PresenceStatementImpl.Definition())
            .addSupport(new KeyStatementImpl.Definition())
            .addSupport(new MaxElementsStatementImpl.Definition())
            .addSupport(new MinElementsStatementImpl.Definition())
            .addSupport(new OrderedByStatementImpl.Definition())
            .addSupport(new WhenStatementImpl.Definition())
            .addSupport(new AugmentStatementImpl.Definition())
            .addSupport(treeScoped(AugmentToChoiceNamespace.class))
            .addSupport(new RefineStatementImpl.Definition())
            .addSupport(new FeatureStatementImpl.Definition())
            .addSupport(new PositionStatementImpl.Definition())
            .addSupport(new ValueStatementImpl.Definition())
            .addSupport(new AnyxmlSchemaLocationStatementImpl.AnyxmlSchemaLocationSupport())
            .addSupport(treeScoped(AnyxmlSchemaLocationNamespace.class))
            .addSupport(global(StmtOrderingNamespace.class))
            .build();

    public static final Map<ModelProcessingPhase, StatementSupportBundle> RFC6020_BUNDLES = ImmutableMap
            .<ModelProcessingPhase, StatementSupportBundle> builder()
            .put(ModelProcessingPhase.INIT, INIT_BUNDLE)
            .put(ModelProcessingPhase.SOURCE_PRE_LINKAGE, PRE_LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.STATEMENT_DEFINITION, STMT_DEF_BUNDLE)
            .put(ModelProcessingPhase.FULL_DECLARATION, FULL_DECL_BUNDLE)
            .put(ModelProcessingPhase.EFFECTIVE_MODEL, FULL_DECL_BUNDLE)
            .build();

    public static final Map<ValidationBundleType, Collection<StatementDefinition>> RFC6020_VALIDATION_BUNDLE = ImmutableMap
            .<ValidationBundleType, Collection<StatementDefinition>> builder()
            .put(ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS, YangValidationBundles.SUPPORTED_REFINE_SUBSTATEMENTS)
            .put(ValidationBundleType.SUPPORTED_AUGMENT_TARGETS, YangValidationBundles.SUPPORTED_AUGMENT_TARGETS)
            .put(ValidationBundleType.SUPPORTED_CASE_SHORTHANDS, YangValidationBundles.SUPPORTED_CASE_SHORTHANDS)
            .put(ValidationBundleType.SUPPORTED_DATA_NODES, YangValidationBundles.SUPPORTED_DATA_NODES)
            .build();

    public static final CrossSourceStatementReactor RFC6020_REACTOR = CrossSourceStatementReactor
            .builder()
            .setBundle(ModelProcessingPhase.INIT, INIT_BUNDLE)
            .setBundle(ModelProcessingPhase.SOURCE_PRE_LINKAGE, PRE_LINKAGE_BUNDLE)
            .setBundle(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
            .setBundle(ModelProcessingPhase.STATEMENT_DEFINITION,
                    STMT_DEF_BUNDLE)
            .setBundle(ModelProcessingPhase.FULL_DECLARATION, FULL_DECL_BUNDLE)
            .setBundle(ModelProcessingPhase.EFFECTIVE_MODEL, FULL_DECL_BUNDLE)
            .setValidationBundle(
                    ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS,
                    YangValidationBundles.SUPPORTED_REFINE_SUBSTATEMENTS)
            .setValidationBundle(ValidationBundleType.SUPPORTED_AUGMENT_TARGETS,
                    YangValidationBundles.SUPPORTED_AUGMENT_TARGETS)
            .setValidationBundle(ValidationBundleType.SUPPORTED_CASE_SHORTHANDS,
                    YangValidationBundles.SUPPORTED_CASE_SHORTHANDS)
            .setValidationBundle(ValidationBundleType.SUPPORTED_DATA_NODES,
                    YangValidationBundles.SUPPORTED_DATA_NODES)
             .build();

    private YangInferencePipeline() {
        throw new UnsupportedOperationException("Utility class");
    }
}
