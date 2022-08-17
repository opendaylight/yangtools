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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ModuleQNameToPrefix;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.YangNamespaceContextNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentImplicitHandlingNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate.DeviateStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate.DeviateStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension.ExtensionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature.IfFeatureStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature.IfFeatureStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_.ImportStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_.ImportedVersionNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list.ConfigListWarningNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list.ListStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ActionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.AnydataStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.AnyxmlStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ArgumentStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.BelongsToStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.BitStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.CaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ChoiceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ConfigStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ContactStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ContainerStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.DefaultStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.DescriptionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.DeviationStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.EnumStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ErrorAppTagStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ErrorMessageStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.FeatureStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.FractionDigitsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.GroupingStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.IdentityStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.IncludeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.InputStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.KeyStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.LeafListStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.LeafStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.LengthStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.MandatoryStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.MaxElementsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.MinElementsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ModifierStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.MustStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.NamespaceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.OrderedByStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.OrganizationStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.OutputStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.PositionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.PrefixStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.PresenceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.RangeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ReferenceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.RequireInstanceStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.RevisionDateStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.RevisionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.RpcStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.StatusStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.TypedefStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.UniqueStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.UnitsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ValueStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.WhenStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.XPathSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.YangVersionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.YinElementStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module.ModuleStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module.QNameModuleNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification.NotificationStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification.NotificationStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path.PathStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern.PatternStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine.RefineStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine.RefineTargetNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule.SubmoduleStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.BaseTypeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.TypeStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.TypeStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses.SourceGroupingNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses.UsesStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.FeatureNamespace;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
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
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * Utility class holding entrypoints for assembling RFC6020/RFC7950 statement {@link CrossSourceStatementReactor}s.
 *
 * @author Robert Varga
 */
@Beta
public final class RFC7950Reactors {
    private static final ImmutableSet<YangVersion> SUPPORTED_VERSIONS = Sets.immutableEnumSet(VERSION_1, VERSION_1_1);

    private static final StatementSupportBundle INIT_BUNDLE = StatementSupportBundle.builder(SUPPORTED_VERSIONS)
        .addSupport(ValidationBundlesNamespace.BEHAVIOUR)
        .addSupport(SupportedFeaturesNamespace.BEHAVIOUR)
        .addSupport(ModulesDeviatedByModules.BEHAVIOUR)
        .build();

    private RFC7950Reactors() {
        // Hidden on purpose
    }

    private static StatementSupportBundle preLinkageBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.derivedFrom(INIT_BUNDLE)
            .addVersionSpecificSupport(VERSION_1, ModuleStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, ModuleStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, SubmoduleStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, SubmoduleStatementSupport.rfc7950Instance(config))
            .addSupport(new NamespaceStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, ImportStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, ImportStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, IncludeStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, IncludeStatementSupport.rfc7950Instance(config))
            .addSupport(new BelongsToStatementSupport(config))
            .addSupport(new PrefixStatementSupport(config))
            .addSupport(new YangVersionStatementSupport(config))
            .addSupport(new RevisionStatementSupport(config))
            .addSupport(new RevisionDateStatementSupport(config))
            .addSupport(ModuleNameToNamespace.BEHAVIOUR)
            .addSupport(PreLinkageModuleNamespace.BEHAVIOUR)
            .addSupport(ImpPrefixToNamespace.BEHAVIOUR)
            .addSupport(ModuleCtxToModuleQName.BEHAVIOUR)
            .addSupport(QNameModuleNamespace.BEHAVIOUR)
            .addSupport(ImportedVersionNamespace.BEHAVIOUR)
            .build();
    }

    private static StatementSupportBundle linkageBundle(final StatementSupportBundle preLinkageBundle,
            final YangParserConfiguration config) {
        return StatementSupportBundle.derivedFrom(preLinkageBundle)
            .addSupport(new DescriptionStatementSupport(config))
            .addSupport(new ReferenceStatementSupport(config))
            .addSupport(new ContactStatementSupport(config))
            .addSupport(new OrganizationStatementSupport(config))
            .addSupport(ModuleNamespace.BEHAVIOUR)
            .addSupport(ModuleNamespaceForBelongsTo.BEHAVIOUR)
            .addSupport(SubmoduleNamespace.BEHAVIOUR)
            .addSupport(NamespaceToModule.BEHAVIOUR)
            .addSupport(ModuleNameToModuleQName.BEHAVIOUR)
            .addSupport(ModuleCtxToSourceIdentifier.BEHAVIOUR)
            .addSupport(ModuleQNameToModuleName.BEHAVIOUR)
            .addSupport(PrefixToModule.BEHAVIOUR)
            .addSupport(ImportedModuleContext.BEHAVIOUR)
            .addSupport(IncludedModuleContext.BEHAVIOUR)
            .addSupport(IncludedSubmoduleNameToModuleCtx.BEHAVIOUR)
            .addSupport(ImportPrefixToModuleCtx.BEHAVIOUR)
            .addSupport(BelongsToPrefixToModuleCtx.BEHAVIOUR)
            .addSupport(ModuleQNameToPrefix.BEHAVIOUR)
            .addSupport(BelongsToPrefixToModuleName.BEHAVIOUR)
            .build();
    }

    private static StatementSupportBundle stmtDefBundle(final StatementSupportBundle linkageBundle,
            final YangParserConfiguration config) {
        return StatementSupportBundle.derivedFrom(linkageBundle)
            .addSupport(new YinElementStatementSupport(config))
            .addSupport(new ArgumentStatementSupport(config))
            .addSupport(new ExtensionStatementSupport(config))
            .addSupport(SchemaTreeNamespace.getInstance())
            .addSupport(ExtensionNamespace.BEHAVIOUR)
            .addSupport(new TypedefStatementSupport(config))
            .addSupport(TypeNamespace.BEHAVIOUR)
            .addVersionSpecificSupport(VERSION_1, IdentityStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, IdentityStatementSupport.rfc7950Instance(config))
            .addSupport(IdentityNamespace.BEHAVIOUR)
            .addSupport(new DefaultStatementSupport(config))
            .addSupport(new StatusStatementSupport(config))
            .addSupport(BaseTypeNamespace.BEHAVIOUR)
            .addVersionSpecificSupport(VERSION_1, new TypeStatementRFC6020Support(config))
            .addVersionSpecificSupport(VERSION_1_1, new TypeStatementRFC7950Support(config))
            .addSupport(new UnitsStatementSupport(config))
            .addSupport(new RequireInstanceStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, BitStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, BitStatementSupport.rfc7950Instance(config))
            .addSupport(PathStatementSupport.strictInstance(config))
            .addVersionSpecificSupport(VERSION_1, EnumStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, EnumStatementSupport.rfc7950Instance(config))
            .addSupport(new LengthStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, PatternStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, PatternStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, new ModifierStatementSupport(config))
            .addSupport(new RangeStatementSupport(config))
            .addSupport(new KeyStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, ContainerStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, ContainerStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, GroupingStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, GroupingStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, ListStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, ListStatementSupport.rfc7950Instance(config))
            .addSupport(ConfigListWarningNamespace.BEHAVIOUR)
            .addSupport(new UniqueStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1_1, new ActionStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, new RpcStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1_1, new RpcStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, InputStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, InputStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, OutputStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, OutputStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, new NotificationStatementRFC6020Support(config))
            .addVersionSpecificSupport(VERSION_1_1, new NotificationStatementRFC7950Support(config))
            .addSupport(new FractionDigitsStatementSupport(config))
            .addSupport(new BaseStatementSupport(config))
            .addSupport(StatementDefinitionNamespace.BEHAVIOUR)
            .build();
    }

    /**
     * Returns a pre-built {@link CrossSourceStatementReactor} supporting RFC6020 and RFC7950, along with OpenConfig
     * semantic version extension. This is useful for parsing near-vanilla YANG models while providing complete
     * support for semantic versions.
     *
     * @return A shared reactor instance.
     */
    public static @NonNull CrossSourceStatementReactor defaultReactor() {
        return ServiceLoaderState.DefaultReactor.INSTANCE;
    }

    /**
     * Returns a partially-configured {@link CustomCrossSourceStatementReactorBuilder}, with RFC6020/RFC7950
     * and OpenConfig semantic version support enabled.
     *
     * @return A new {@link CustomCrossSourceStatementReactorBuilder}.
     */
    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder() {
        return defaultReactorBuilder(YangParserConfiguration.DEFAULT);
    }

    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder(
            final YangParserConfiguration config) {
        return vanillaReactorBuilder(config);
    }

    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder(
            final YangXPathParserFactory xpathFactory) {
        return defaultReactorBuilder(xpathFactory, YangParserConfiguration.DEFAULT);
    }

    public static @NonNull CustomCrossSourceStatementReactorBuilder defaultReactorBuilder(
            final YangXPathParserFactory xpathFactory, final YangParserConfiguration config) {
        return vanillaReactorBuilder(xpathFactory, config);
    }

    /**
     * Returns a pre-built {@link CrossSourceStatementReactor} supporting both RFC6020 and RFC7950. This is useful
     * for parsing vanilla YANG models without any semantic support for extensions. Notably missing is the semantic
     * version extension, hence attempts to use semantic version mode will cause failures.
     *
     * @return A shared reactor instance.
     */
    public static @NonNull CrossSourceStatementReactor vanillaReactor() {
        return ServiceLoaderState.VanillaReactor.INSTANCE;
    }

    /**
     * Returns a partially-configured {@link CustomCrossSourceStatementReactorBuilder}, with vanilla RFC6020/RFC7950
     * support enabled.
     *
     * @return A new {@link CustomCrossSourceStatementReactorBuilder}.
     */
    public static @NonNull CustomCrossSourceStatementReactorBuilder vanillaReactorBuilder() {
        return vanillaReactorBuilder(YangParserConfiguration.DEFAULT);
    }

    public static @NonNull CustomCrossSourceStatementReactorBuilder vanillaReactorBuilder(
            final YangParserConfiguration config) {
        return vanillaReactorBuilder(ServiceLoaderState.XPath.INSTANCE, config);
    }

    public static @NonNull CustomCrossSourceStatementReactorBuilder vanillaReactorBuilder(
            final @NonNull YangXPathParserFactory xpathFactory) {
        return vanillaReactorBuilder(xpathFactory, YangParserConfiguration.DEFAULT);
    }

    public static @NonNull CustomCrossSourceStatementReactorBuilder vanillaReactorBuilder(
            final @NonNull YangXPathParserFactory xpathFactory, final YangParserConfiguration config) {
        return vanillaReactorBuilder(new XPathSupport(xpathFactory), config);
    }

    private static @NonNull CustomCrossSourceStatementReactorBuilder vanillaReactorBuilder(
            final @NonNull XPathSupport xpathSupport, final YangParserConfiguration config) {
        final StatementSupportBundle preLinkageBundle = preLinkageBundle(config);
        final StatementSupportBundle linkageBundle = linkageBundle(preLinkageBundle, config);
        final StatementSupportBundle stmtDefBundle = stmtDefBundle(linkageBundle, config);
        final StatementSupportBundle fullDeclarationBundle =
            fullDeclarationBundle(stmtDefBundle, xpathSupport, config);

        return new CustomCrossSourceStatementReactorBuilder(SUPPORTED_VERSIONS)
                .addAllSupports(ModelProcessingPhase.INIT, INIT_BUNDLE)
                .addAllSupports(ModelProcessingPhase.SOURCE_PRE_LINKAGE, preLinkageBundle)
                .addAllSupports(ModelProcessingPhase.SOURCE_LINKAGE, linkageBundle)
                .addAllSupports(ModelProcessingPhase.STATEMENT_DEFINITION, stmtDefBundle)
                .addAllSupports(ModelProcessingPhase.FULL_DECLARATION, fullDeclarationBundle)
                .addAllSupports(ModelProcessingPhase.EFFECTIVE_MODEL, fullDeclarationBundle)
                .addValidationBundle(ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS,
                    YangValidationBundles.SUPPORTED_REFINE_SUBSTATEMENTS)
                .addValidationBundle(ValidationBundleType.SUPPORTED_AUGMENT_TARGETS,
                    YangValidationBundles.SUPPORTED_AUGMENT_TARGETS)

                // FIXME: 7.0.0: we do not seem to need this validation bundle
                .addValidationBundle(ValidationBundleType.SUPPORTED_CASE_SHORTHANDS,
                    YangValidationBundles.SUPPORTED_CASE_SHORTHANDS)

                .addValidationBundle(ValidationBundleType.SUPPORTED_DATA_NODES,
                    YangValidationBundles.SUPPORTED_DATA_NODES);
    }

    private static @NonNull StatementSupportBundle fullDeclarationBundle(final StatementSupportBundle stmtDefBundle,
            final XPathSupport xpathSupport, final YangParserConfiguration config) {
        return StatementSupportBundle.derivedFrom(stmtDefBundle)
            .addSupport(new LeafStatementSupport(config))
            .addSupport(new ConfigStatementSupport(config))
            .addSupport(new DeviationStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, new DeviateStatementRFC6020Support(config))
            .addVersionSpecificSupport(VERSION_1_1, new DeviateStatementRFC7950Support(config))
            .addVersionSpecificSupport(VERSION_1, ChoiceStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, ChoiceStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, CaseStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, CaseStatementSupport.rfc7950Instance(config))
            .addSupport(new MustStatementSupport(xpathSupport, config))
            .addSupport(new MandatoryStatementSupport(config))
            .addSupport(new AnyxmlStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1_1, new AnydataStatementSupport(config))
            .addSupport(FeatureNamespace.BEHAVIOUR)
            .addVersionSpecificSupport(VERSION_1, new IfFeatureStatementRFC6020Support(config))
            .addVersionSpecificSupport(VERSION_1_1, new IfFeatureStatementRFC7950Support(config))
            .addSupport(GroupingNamespace.BEHAVIOUR)
            .addSupport(SourceGroupingNamespace.BEHAVIOUR)
            .addSupport(new UsesStatementSupport(config))
            .addSupport(new ErrorMessageStatementSupport(config))
            .addSupport(new ErrorAppTagStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, LeafListStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, LeafListStatementSupport.rfc7950Instance(config))
            .addSupport(new PresenceStatementSupport(config))
            .addSupport(new MaxElementsStatementSupport(config))
            .addSupport(new MinElementsStatementSupport(config))
            .addSupport(new OrderedByStatementSupport(config))
            .addSupport(new WhenStatementSupport(xpathSupport, config))
            .addSupport(AugmentImplicitHandlingNamespace.BEHAVIOUR)
            .addVersionSpecificSupport(VERSION_1, new AugmentStatementRFC6020Support(config))
            .addVersionSpecificSupport(VERSION_1_1, new AugmentStatementRFC7950Support(config))
            .addSupport(RefineTargetNamespace.BEHAVIOUR)
            .addVersionSpecificSupport(VERSION_1, RefineStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, RefineStatementSupport.rfc7950Instance(config))
            .addSupport(new FeatureStatementSupport(config))
            .addSupport(new PositionStatementSupport(config))
            .addSupport(new ValueStatementSupport(config))
            .addSupport(YangNamespaceContextNamespace.BEHAVIOUR)
            .build();
    }
}
