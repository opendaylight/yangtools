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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.action.ActionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata.AnydataStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.argument.ArgumentStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.base.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.belongs_to.BelongsToStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit.BitStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit.BitStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.config.ConfigStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.contact.ContactStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.default_.DefaultStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.description.DescriptionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation.DeviationStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_.EnumStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_.EnumStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_app_tag.ErrorAppTagStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_message.ErrorMessageStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.feature.FeatureStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.fraction_digits.FractionDigitsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping.GroupingStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping.GroupingStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity.IdentityStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity.IdentityStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature.IfFeatureStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_.ImportStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_.ImportStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.include.IncludeStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.include.IncludeStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key.KeyStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.length.LengthStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.mandatory.MandatoryStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.max_elements.MaxElementsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.min_elements.MinElementsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.modifier.ModifierStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module.ModuleStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module.ModuleStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.must.MustStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.namespace.NamespaceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification.NotificationStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification.NotificationStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ordered_by.OrderedByStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.organization.OrganizationStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path.PathStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern.PatternStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern.PatternStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.position.PositionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.prefix.PrefixStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.presence.PresenceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.reference.ReferenceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine.RefineStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine.RefineStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.require_instance.RequireInstanceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision.RevisionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision_date.RevisionDateStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc.RpcStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc.RpcStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.status.StatusStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule.SubmoduleStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule.SubmoduleStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef.TypedefStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique.UniqueStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.units.UnitsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.value.ValueStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.when.WhenStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yang_version.YangVersionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yin_element.YinElementStatementSupport;
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
import org.opendaylight.yangtools.yang.parser.spi.source.AugmentToChoiceNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToSemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToSourceIdentifier;
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
import org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema.AnyxmlSchemaLocationNamespace;
import org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema.AnyxmlSchemaLocationSupport;
import org.opendaylight.yangtools.yang.parser.stmt.openconfig.OpenconfigVersionSupport;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.Builder;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.CaseStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ChoiceStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ContainerStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.DeviateStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.LeafListStatementRfc7950Support;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.ListStatementRfc7950Support;
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
            .addVersionSpecificSupport(VERSION_1, new ModuleStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new ModuleStatementRFC7950Support())
            .addVersionSpecificSupport(VERSION_1, new SubmoduleStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new SubmoduleStatementRFC7950Support())
            .addSupport(new NamespaceStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new ImportStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new ImportStatementRFC7950Support())
            .addVersionSpecificSupport(VERSION_1, new IncludeStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new IncludeStatementRFC7950Support())
            .addSupport(new BelongsToStatementSupport())
            .addSupport(new PrefixStatementSupport())
            .addSupport(new YangVersionStatementSupport())
            .addSupport(new RevisionStatementSupport())
            .addSupport(new RevisionDateStatementSupport())
            .addSupport(global(ModuleNameToNamespace.class))
            .addSupport(global(PreLinkageModuleNamespace.class))
            .addSupport(sourceLocal(ImpPrefixToNamespace.class))
            .addSupport(global(ModuleCtxToModuleQName.class))
            .build();

    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle
            .derivedFrom(PRE_LINKAGE_BUNDLE)
            .addSupport(new DescriptionStatementSupport())
            .addSupport(new ReferenceStatementSupport())
            .addSupport(new ContactStatementSupport())
            .addSupport(new OrganizationStatementSupport())
            .addSupport(global(ModuleNamespace.class))
            .addSupport(global(ModuleNamespaceForBelongsTo.class))
            .addSupport(global(SubmoduleNamespace.class))
            .addSupport(global(NamespaceToModule.class))
            .addSupport(global(ModuleNameToModuleQName.class))
            .addSupport(global(ModuleCtxToSourceIdentifier.class))
            .addSupport(global(ModuleQNameToModuleName.class))
            .addSupport(global(PrefixToModule.class))
            .addSupport(QNameCacheNamespace.getInstance())
            .addSupport(sourceLocal(ImportedModuleContext.class))
            .addSupport(sourceLocal(IncludedModuleContext.class))
            .addSupport(sourceLocal(IncludedSubmoduleNameToModuleCtx.class))
            .addSupport(sourceLocal(ImportPrefixToModuleCtx.class))
            .addSupport(sourceLocal(BelongsToPrefixToModuleCtx.class))
            .addSupport(sourceLocal(URIStringToImpPrefix.class))
            .addSupport(sourceLocal(BelongsToModuleContext.class))
            .addSupport(sourceLocal(QNameToStatementDefinition.class))
            .addSupport(sourceLocal(BelongsToPrefixToModuleName.class))
            .addSupport(OpenconfigVersionSupport.getInstance())
            .addSupport(global(SemanticVersionNamespace.class))
            .addSupport(global(SemanticVersionModuleNamespace.class))
            .addSupport(sourceLocal(ImportPrefixToSemVerSourceIdentifier.class))
            .build();

    public static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle
            .derivedFrom(LINKAGE_BUNDLE)
            .addSupport(new YinElementStatementSupport())
            .addSupport(new ArgumentStatementSupport())
            .addSupport(new ExtensionStatementImpl.Definition())
            .addSupport(new ChildSchemaNodes<>())
            .addSupport(new SchemaNodeIdentifierBuildNamespace())
            .addSupport(global(ExtensionNamespace.class))
            .addSupport(new TypedefStatementSupport())
            .addSupport(treeScoped(TypeNamespace.class))
            .addVersionSpecificSupport(VERSION_1, new IdentityStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new IdentityStatementRFC7950Support())
            .addSupport(global(IdentityNamespace.class))
            .addSupport(new DefaultStatementSupport())
            .addSupport(new StatusStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new TypeStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new TypeStatementRfc7950Support())
            .addSupport(new UnitsStatementSupport())
            .addSupport(new RequireInstanceStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new BitStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new BitStatementRFC7950Support())
            .addSupport(new PathStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new EnumStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new EnumStatementRFC7950Support())
            .addSupport(new LengthStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new PatternStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new PatternStatementRFC7950Support())
            .addVersionSpecificSupport(VERSION_1_1, new ModifierStatementSupport())
            .addSupport(new RangeStatementImpl.Definition())
            .addSupport(new KeyStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new ContainerStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new ContainerStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new GroupingStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new GroupingStatementRFC7950Support())
            .addVersionSpecificSupport(VERSION_1, new ListStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new ListStatementRfc7950Support())
            .addSupport(new UniqueStatementSupport())
            .addVersionSpecificSupport(VERSION_1_1, new ActionStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new RpcStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new RpcStatementRFC7950Support())
            .addVersionSpecificSupport(VERSION_1, new InputStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new InputStatementRFC7950Support())
            .addVersionSpecificSupport(VERSION_1, new OutputStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new OutputStatementRFC7950Support())
            .addVersionSpecificSupport(VERSION_1, new NotificationStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new NotificationStatementRFC7950Support())
            .addSupport(new FractionDigitsStatementSupport())
            .addSupport(new BaseStatementSupport())
            .addSupport(global(DerivedIdentitiesNamespace.class))
            .addSupport(global(StatementDefinitionNamespace.class))
            .build();

    public static final StatementSupportBundle FULL_DECL_BUNDLE = StatementSupportBundle
            .derivedFrom(STMT_DEF_BUNDLE)
            .addSupport(new LeafStatementImpl.Definition())
            .addSupport(new ConfigStatementSupport())
            .addSupport(new DeviationStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new DeviateStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new DeviateStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new ChoiceStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new ChoiceStatementRfc7950Support())
            .addVersionSpecificSupport(VERSION_1, new CaseStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new CaseStatementRfc7950Support())
            .addSupport(new MustStatementSupport())
            .addSupport(new MandatoryStatementSupport())
            .addSupport(new AnyxmlStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new AnydataStatementSupport())
            .addSupport(new IfFeatureStatementSupport())
            .addSupport(new UsesStatementImpl.Definition())
            .addSupport(treeScoped(GroupingNamespace.class)) //treeScoped
            .addSupport(new ErrorMessageStatementSupport())
            .addSupport(new ErrorAppTagStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new LeafListStatementImpl.Definition())
            .addVersionSpecificSupport(VERSION_1_1, new LeafListStatementRfc7950Support())
            .addSupport(new PresenceStatementSupport())
            .addSupport(new MaxElementsStatementSupport())
            .addSupport(new MinElementsStatementSupport())
            .addSupport(new OrderedByStatementSupport())
            .addSupport(new WhenStatementSupport())
            .addVersionSpecificSupport(VERSION_1, new AugmentStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new AugmentStatementRFC7950Support())
            .addSupport(treeScoped(AugmentToChoiceNamespace.class))
            .addVersionSpecificSupport(VERSION_1, new RefineStatementRFC6020Support())
            .addVersionSpecificSupport(VERSION_1_1, new RefineStatementRFC7950Support())
            .addSupport(new FeatureStatementSupport())
            .addSupport(new PositionStatementSupport())
            .addSupport(new ValueStatementSupport())
            .addSupport(AnyxmlSchemaLocationSupport.getInstance())
            .addSupport(treeScoped(AnyxmlSchemaLocationNamespace.class))
            .addSupport(YangDataStatementSupport.getInstance())
            .addSupport(global(StmtOrderingNamespace.class))
            .build();

    public static final Map<ModelProcessingPhase, StatementSupportBundle> RFC6020_BUNDLES =
            ImmutableMap.<ModelProcessingPhase, StatementSupportBundle>builder()
            .put(ModelProcessingPhase.INIT, INIT_BUNDLE)
            .put(ModelProcessingPhase.SOURCE_PRE_LINKAGE, PRE_LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.STATEMENT_DEFINITION, STMT_DEF_BUNDLE)
            .put(ModelProcessingPhase.FULL_DECLARATION, FULL_DECL_BUNDLE)
            .put(ModelProcessingPhase.EFFECTIVE_MODEL, FULL_DECL_BUNDLE)
            .build();

    public static final Map<ValidationBundleType, Collection<StatementDefinition>> RFC6020_VALIDATION_BUNDLE =
            ImmutableMap.<ValidationBundleType, Collection<StatementDefinition>>builder()
            .put(ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS,
                YangValidationBundles.SUPPORTED_REFINE_SUBSTATEMENTS)
            .put(ValidationBundleType.SUPPORTED_AUGMENT_TARGETS, YangValidationBundles.SUPPORTED_AUGMENT_TARGETS)
            .put(ValidationBundleType.SUPPORTED_CASE_SHORTHANDS, YangValidationBundles.SUPPORTED_CASE_SHORTHANDS)
            .put(ValidationBundleType.SUPPORTED_DATA_NODES, YangValidationBundles.SUPPORTED_DATA_NODES)
            .build();

    public static Builder newReactorBuilder() {
        return CrossSourceStatementReactor.builder()
                .setBundle(ModelProcessingPhase.INIT, INIT_BUNDLE)
                .setBundle(ModelProcessingPhase.SOURCE_PRE_LINKAGE, PRE_LINKAGE_BUNDLE)
                .setBundle(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
                .setBundle(ModelProcessingPhase.STATEMENT_DEFINITION, STMT_DEF_BUNDLE)
                .setBundle(ModelProcessingPhase.FULL_DECLARATION, FULL_DECL_BUNDLE)
                .setBundle(ModelProcessingPhase.EFFECTIVE_MODEL, FULL_DECL_BUNDLE)
                .setValidationBundle(ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS,
                    YangValidationBundles.SUPPORTED_REFINE_SUBSTATEMENTS)
                .setValidationBundle(ValidationBundleType.SUPPORTED_AUGMENT_TARGETS,
                    YangValidationBundles.SUPPORTED_AUGMENT_TARGETS)
                .setValidationBundle(ValidationBundleType.SUPPORTED_CASE_SHORTHANDS,
                    YangValidationBundles.SUPPORTED_CASE_SHORTHANDS)
                .setValidationBundle(ValidationBundleType.SUPPORTED_DATA_NODES,
                    YangValidationBundles.SUPPORTED_DATA_NODES);
    }

    private YangInferencePipeline() {
        throw new UnsupportedOperationException("Utility class");
    }
}
