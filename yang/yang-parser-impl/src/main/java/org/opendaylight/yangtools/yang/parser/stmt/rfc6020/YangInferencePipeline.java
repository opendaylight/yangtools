/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1;
import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1_1;
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
import org.opendaylight.yangtools.yang.parser.spi.source.ModulesDeviatedByModules;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StmtOrderingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ActionStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.AnydataStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.AugmentStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.BitStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.CaseStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ChoiceStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ContainerStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.DeviateStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.EnumStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.GroupingStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.IdentityStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ImportStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.IncludeStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.InputStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.LeafListStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ListStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ModifierStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ModuleStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.NotificationStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.OutputStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.PatternStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.RefineStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.RpcStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.SubmoduleStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.TypeStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc8040.YangDataStatementSupport;

public final class YangInferencePipeline {
    public static final Set<YangVersion> SUPPORTED_VERSIONS = Sets.immutableEnumSet(VERSION_1, VERSION_1_1);

    public static final StatementSupportBundle INIT_BUNDLE = StatementSupportBundle
            .builder(SUPPORTED_VERSIONS).addSupport(global(ValidationBundlesNamespace.class))
            .addSupport(global(SupportedFeaturesNamespace.class))
            .addSupport(global(ModulesDeviatedByModules.class))
            .build();

    public static final StatementSupportBundle PRE_LINKAGE_BUNDLE = StatementSupportBundle
            .derivedFrom(INIT_BUNDLE)
            .addVersionSpecificSupport(VERSION_1, new ModuleStatementSupport())
            .addVersionSpecificSupport(VERSION_1_1, new ModuleStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new SubmoduleStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new SubmoduleStatementRfc7950Support())
            .addSupport(new NamespaceStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new ImportStatementDefinition())
            .addVersionSpecificSupport(VERSION_1_1, new ImportStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new IncludeStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new IncludeStatementRfc7950Support())
            .addSupport(new PrefixStatementImpl.Definition())
            .addSupport(new YangVersionStatementImpl.Definition())
            .addSupport(new RevisionStatementImpl.Definition())
            .addSupport(new RevisionDateStatementImpl.Definition())
            .addSupport(global(ModuleNameToNamespace.class))
            .addSupport(global(PreLinkageModuleNamespace.class))
            .addSupport(sourceLocal(ImpPrefixToNamespace.class))
            .addSupport(global(ModuleCtxToModuleQName.class))
            .build();

    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle
            .derivedFrom(PRE_LINKAGE_BUNDLE)
            .addSupport(new DescriptionStatementImpl.Definition())
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
            .addSupport(new OpenconfigVersionStatementImpl.OpenconfigVersionSupport())
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
            .addVersionSpecificSupport(VERSION_1, new IdentityStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new IdentityStatementRfc7950Support())
            .addSupport(global(IdentityNamespace.class))
            .addSupport(new DefaultStatementImpl.Definition())
            .addSupport(new StatusStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new TypeStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new TypeStatementRfc7950Support())
            .addSupport(new UnitsStatementImpl.Definition())
            .addSupport(new RequireInstanceStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new BitStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new BitStatementRfc7950Support())
            .addSupport(new PathStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new EnumStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new EnumStatementRfc7950Support())
            .addSupport(new LengthStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new PatternStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new PatternStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1_1, new ModifierStatementImpl.Definition())
            .addSupport(new RangeStatementImpl.Definition())
            .addSupport(new KeyStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new ContainerStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new ContainerStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new GroupingStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new GroupingStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new ListStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new ListStatementRfc7950Support())
            .addSupport(new UniqueStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new ActionStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new RpcStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new RpcStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new InputStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new InputStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new OutputStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new OutputStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new NotificationStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new NotificationStatementRfc7950Support())
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
            .addVersionSpecificSupport(VERSION_1, new DeviateStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new DeviateStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new ChoiceStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new ChoiceStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new CaseStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new CaseStatementRfc7950Support())
            .addSupport(new MustStatementImpl.Definition())
            .addSupport(new MandatoryStatementImpl.Definition())
            .addSupport(new AnyxmlStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new AnydataStatementImpl.Definition())
            .addSupport(new IfFeatureStatementImpl.Definition())
            .addSupport(new UsesStatementImpl.Definition())
            .addSupport(treeScoped(GroupingNamespace.class)) //treeScoped
            .addSupport(new ErrorMessageStatementImpl.Definition())
            .addSupport(new ErrorAppTagStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new LeafListStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new LeafListStatementRfc7950Support())
            .addSupport(new PresenceStatementImpl.Definition())
            .addSupport(new MaxElementsStatementImpl.Definition())
            .addSupport(new MinElementsStatementImpl.Definition())
            .addSupport(new OrderedByStatementImpl.Definition())
            .addSupport(new WhenStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1, new AugmentStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new AugmentStatementRfc7950Support())
            .addSupport(treeScoped(AugmentToChoiceNamespace.class))
            .addVersionSpecificSupport(VERSION_1, new RefineStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new RefineStatementRfc7950Support())
            .addSupport(new FeatureStatementImpl.Definition())
            .addSupport(new PositionStatementImpl.Definition())
            .addSupport(new ValueStatementImpl.Definition())
            .addSupport(new AnyxmlSchemaLocationStatementImpl.AnyxmlSchemaLocationSupport())
            .addSupport(treeScoped(AnyxmlSchemaLocationNamespace.class))
            .addSupport(YangDataStatementSupport.getInstance())
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
