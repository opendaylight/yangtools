/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.reactor;

import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1;
import static org.opendaylight.yangtools.yang.common.YangVersion.VERSION_1_1;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.openconfig.stmt.OpenConfigVersionSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.SchemaNodeIdentifierBuildNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.URIStringToImportPrefix;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.action.ActionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata.AnydataStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml.AnyxmlStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.argument.ArgumentStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.base.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.belongs_to.BelongsToStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit.BitStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit.BitStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_.CaseStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_.CaseStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice.ChoiceStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice.ChoiceStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.config.ConfigStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.contact.ContactStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container.ContainerStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container.ContainerStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.default_.DefaultStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.description.DescriptionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate.DeviateStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate.DeviateStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation.DeviationStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_.EnumStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_.EnumStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_app_tag.ErrorAppTagStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_message.ErrorMessageStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension.ExtensionStatementSupport;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf.LeafStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list.LeafListStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list.LeafListStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.length.LengthStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list.ListStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list.ListStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.mandatory.MandatoryStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.max_elements.MaxElementsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.min_elements.MinElementsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.modifier.ModifierStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module.ModuleStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module.ModuleStatementRFC7950Support;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.range.RangeStatementSupport;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.TypeStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.TypeStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef.TypedefStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique.UniqueStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.units.UnitsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses.UsesStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.value.ValueStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.when.WhenStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yang_version.YangVersionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.yin_element.YinElementStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.FeatureNamespace;
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
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

/**
 * Utility class holding entrypoints for assembling RFC6020/RFC7950 statement {@link CrossSourceStatementReactor}s.
 *
 * @author Robert Varga
 */
@Beta
public final class RFC7950Reactors {
    private static final Set<YangVersion> SUPPORTED_VERSIONS = Sets.immutableEnumSet(VERSION_1, VERSION_1_1);

    private static final StatementSupportBundle INIT_BUNDLE = StatementSupportBundle.builder(SUPPORTED_VERSIONS)
            .addSupport(ValidationBundlesNamespace.BEHAVIOUR)
            .addSupport(SupportedFeaturesNamespace.BEHAVIOUR)
            .addSupport(ModulesDeviatedByModules.BEHAVIOUR)
            .build();

    private static final StatementSupportBundle PRE_LINKAGE_BUNDLE = StatementSupportBundle.derivedFrom(INIT_BUNDLE)
            .addVersionSpecificSupport(VERSION_1, ModuleStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, ModuleStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, SubmoduleStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, SubmoduleStatementRFC7950Support.getInstance())
            .addSupport(NamespaceStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, ImportStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, ImportStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, IncludeStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, IncludeStatementRFC7950Support.getInstance())
            .addSupport(BelongsToStatementSupport.getInstance())
            .addSupport(PrefixStatementSupport.getInstance())
            .addSupport(YangVersionStatementSupport.getInstance())
            .addSupport(RevisionStatementSupport.getInstance())
            .addSupport(RevisionDateStatementSupport.getInstance())
            .addSupport(ModuleNameToNamespace.BEHAVIOUR)
            .addSupport(PreLinkageModuleNamespace.BEHAVIOUR)
            .addSupport(ImpPrefixToNamespace.BEHAVIOUR)
            .addSupport(ModuleCtxToModuleQName.BEHAVIOUR)
            .build();

    private static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle
            .derivedFrom(PRE_LINKAGE_BUNDLE)
            .addSupport(DescriptionStatementSupport.getInstance())
            .addSupport(ReferenceStatementSupport.getInstance())
            .addSupport(ContactStatementSupport.getInstance())
            .addSupport(OrganizationStatementSupport.getInstance())
            .addSupport(ModuleNamespace.BEHAVIOUR)
            .addSupport(ModuleNamespaceForBelongsTo.BEHAVIOUR)
            .addSupport(SubmoduleNamespace.BEHAVIOUR)
            .addSupport(NamespaceToModule.BEHAVIOUR)
            .addSupport(ModuleNameToModuleQName.BEHAVIOUR)
            .addSupport(ModuleCtxToSourceIdentifier.BEHAVIOUR)
            .addSupport(ModuleQNameToModuleName.BEHAVIOUR)
            .addSupport(PrefixToModule.BEHAVIOUR)
            .addSupport(QNameCacheNamespace.getInstance())
            .addSupport(ImportedModuleContext.BEHAVIOUR)
            .addSupport(IncludedModuleContext.BEHAVIOUR)
            .addSupport(IncludedSubmoduleNameToModuleCtx.BEHAVIOUR)
            .addSupport(ImportPrefixToModuleCtx.BEHAVIOUR)
            .addSupport(BelongsToPrefixToModuleCtx.BEHAVIOUR)
            .addSupport(URIStringToImportPrefix.BEHAVIOUR)
            .addSupport(BelongsToModuleContext.BEHAVIOUR)
            .addSupport(QNameToStatementDefinition.BEHAVIOUR)
            .addSupport(BelongsToPrefixToModuleName.BEHAVIOUR)
            .build();

    private static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle
            .derivedFrom(LINKAGE_BUNDLE)
            .addSupport(YinElementStatementSupport.getInstance())
            .addSupport(ArgumentStatementSupport.getInstance())
            .addSupport(ExtensionStatementSupport.getInstance())
            .addSupport(new ChildSchemaNodeNamespace<>())
            .addSupport(new SchemaNodeIdentifierBuildNamespace())
            .addSupport(ExtensionNamespace.BEHAVIOUR)
            .addSupport(TypedefStatementSupport.getInstance())
            .addSupport(TypeNamespace.BEHAVIOUR)
            .addVersionSpecificSupport(VERSION_1, IdentityStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, IdentityStatementRFC7950Support.getInstance())
            .addSupport(IdentityNamespace.BEHAVIOUR)
            .addSupport(DefaultStatementSupport.getInstance())
            .addSupport(StatusStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, TypeStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, TypeStatementRFC7950Support.getInstance())
            .addSupport(UnitsStatementSupport.getInstance())
            .addSupport(RequireInstanceStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, BitStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, BitStatementRFC7950Support.getInstance())
            .addSupport(PathStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, EnumStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, EnumStatementRFC7950Support.getInstance())
            .addSupport(LengthStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, PatternStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, PatternStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, ModifierStatementSupport.getInstance())
            .addSupport(RangeStatementSupport.getInstance())
            .addSupport(KeyStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, ContainerStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, ContainerStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, GroupingStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, GroupingStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, ListStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, ListStatementRFC7950Support.getInstance())
            .addSupport(UniqueStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, ActionStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, RpcStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, RpcStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, InputStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, InputStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, OutputStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, OutputStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, NotificationStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, NotificationStatementRFC7950Support.getInstance())
            .addSupport(FractionDigitsStatementSupport.getInstance())
            .addSupport(BaseStatementSupport.getInstance())
            .addSupport(DerivedIdentitiesNamespace.BEHAVIOUR)
            .addSupport(StatementDefinitionNamespace.BEHAVIOUR)
            .build();

    private static final StatementSupportBundle FULL_DECL_BUNDLE = StatementSupportBundle
            .derivedFrom(STMT_DEF_BUNDLE)
            .addSupport(LeafStatementSupport.getInstance())
            .addSupport(ConfigStatementSupport.getInstance())
            .addSupport(DeviationStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, DeviateStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, DeviateStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, ChoiceStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, ChoiceStatementRFC7950Support.getInstance())
            .addVersionSpecificSupport(VERSION_1, CaseStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, CaseStatementRFC7950Support.getInstance())
            .addSupport(MustStatementSupport.getInstance())
            .addSupport(MandatoryStatementSupport.getInstance())
            .addSupport(AnyxmlStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, AnydataStatementSupport.getInstance())
            .addSupport(FeatureNamespace.BEHAVIOUR)
            .addSupport(IfFeatureStatementSupport.getInstance())
            .addSupport(UsesStatementSupport.getInstance())
            .addSupport(GroupingNamespace.BEHAVIOUR)
            .addSupport(ErrorMessageStatementSupport.getInstance())
            .addSupport(ErrorAppTagStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, LeafListStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, LeafListStatementRFC7950Support.getInstance())
            .addSupport(PresenceStatementSupport.getInstance())
            .addSupport(MaxElementsStatementSupport.getInstance())
            .addSupport(MinElementsStatementSupport.getInstance())
            .addSupport(OrderedByStatementSupport.getInstance())
            .addSupport(WhenStatementSupport.getInstance())
            .addVersionSpecificSupport(VERSION_1, AugmentStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, AugmentStatementRFC7950Support.getInstance())
            .addSupport(AugmentToChoiceNamespace.BEHAVIOUR)
            .addVersionSpecificSupport(VERSION_1, RefineStatementRFC6020Support.getInstance())
            .addVersionSpecificSupport(VERSION_1_1, RefineStatementRFC7950Support.getInstance())
            .addSupport(FeatureStatementSupport.getInstance())
            .addSupport(PositionStatementSupport.getInstance())
            .addSupport(ValueStatementSupport.getInstance())
            .addSupport(StmtOrderingNamespace.BEHAVIOUR)
            .build();

    private static final Map<ModelProcessingPhase, StatementSupportBundle> RFC7950_BUNDLES =
            ImmutableMap.<ModelProcessingPhase, StatementSupportBundle>builder()
            .put(ModelProcessingPhase.INIT, INIT_BUNDLE)
            .put(ModelProcessingPhase.SOURCE_PRE_LINKAGE, PRE_LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.STATEMENT_DEFINITION, STMT_DEF_BUNDLE)
            .put(ModelProcessingPhase.FULL_DECLARATION, FULL_DECL_BUNDLE)
            .put(ModelProcessingPhase.EFFECTIVE_MODEL, FULL_DECL_BUNDLE)
            .build();

    private static final Map<ValidationBundleType, Collection<StatementDefinition>> RFC6020_VALIDATION_BUNDLE =
            ImmutableMap.<ValidationBundleType, Collection<StatementDefinition>>builder()
            .put(ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS,
                YangValidationBundles.SUPPORTED_REFINE_SUBSTATEMENTS)
            .put(ValidationBundleType.SUPPORTED_AUGMENT_TARGETS, YangValidationBundles.SUPPORTED_AUGMENT_TARGETS)
            .put(ValidationBundleType.SUPPORTED_CASE_SHORTHANDS, YangValidationBundles.SUPPORTED_CASE_SHORTHANDS)
            .put(ValidationBundleType.SUPPORTED_DATA_NODES, YangValidationBundles.SUPPORTED_DATA_NODES)
            .build();

    private static final CrossSourceStatementReactor DEFAULT_RFC6020_RFC7950_REACTOR = defaultReactorBuilder().build();
    private static final CrossSourceStatementReactor VANILLA_RFC6020_RFC7950_REACTOR = vanillaReactorBuilder().build();

    private RFC7950Reactors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a pre-built {@link CrossSourceStatementReactor} supporting RFC6020 and RFC7950, along with OpenConfig
     * semantic version extension. This is useful for parsing near-vanilla YANG models while providing complete
     * support for semantic versions.
     *
     * @return A shared reactor instance.
     */
    public static CrossSourceStatementReactor defaultReactor() {
        return DEFAULT_RFC6020_RFC7950_REACTOR;
    }

    /**
     * Returns a partially-configured {@link CustomCrossSourceStatementReactorBuilder}, with RFC6020/RFC7950
     * and OpenConfig semantic version support enabled.
     *
     * @return A new {@link CustomCrossSourceStatementReactorBuilder}.
     */
    public static CustomCrossSourceStatementReactorBuilder defaultReactorBuilder() {
        return vanillaReactorBuilder()
                // Semantic version support
                .addStatementSupport(ModelProcessingPhase.SOURCE_LINKAGE, OpenConfigVersionSupport.getInstance())
                .addNamespaceSupport(ModelProcessingPhase.SOURCE_LINKAGE, SemanticVersionNamespace.BEHAVIOUR)
                .addNamespaceSupport(ModelProcessingPhase.SOURCE_LINKAGE, SemanticVersionModuleNamespace.BEHAVIOUR)
                .addNamespaceSupport(ModelProcessingPhase.SOURCE_LINKAGE,
                    ImportPrefixToSemVerSourceIdentifier.BEHAVIOUR);
    }

    /**
     * Returns a pre-built {@link CrossSourceStatementReactor} supporting both RFC6020 and RFC7950. This is useful
     * for parsing vanilla YANG models without any semantic support for extensions. Notably missing is the semantic
     * version extension, hence attempts to use semantic version mode will cause failures.
     *
     * @return A shared reactor instance.
     */
    public static CrossSourceStatementReactor vanillaReactor() {
        return VANILLA_RFC6020_RFC7950_REACTOR;
    }

    /**
     * Returns a partially-configured {@link CustomCrossSourceStatementReactorBuilder}, with vanilla RFC6020/RFC7950
     * support enabled.
     *
     * @return A new {@link CustomCrossSourceStatementReactorBuilder}.
     */
    public static CustomCrossSourceStatementReactorBuilder vanillaReactorBuilder() {
        return addDefaultRFC7950Bundles(new CustomCrossSourceStatementReactorBuilder(SUPPORTED_VERSIONS));
    }

    private static CustomCrossSourceStatementReactorBuilder addDefaultRFC7950Bundles(
            final CustomCrossSourceStatementReactorBuilder builder) {
        RFC7950_BUNDLES.entrySet().forEach(entry -> builder.addAllSupports(entry.getKey(), entry.getValue()));
        RFC6020_VALIDATION_BUNDLE.entrySet().forEach(
            entry -> builder.addValidationBundle(entry.getKey(), entry.getValue()));
        return builder;
    }
}
