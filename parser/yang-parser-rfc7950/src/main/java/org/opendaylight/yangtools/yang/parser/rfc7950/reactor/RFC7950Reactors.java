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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.YangNamespaceContextNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentImplicitHandlingNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment.AugmentStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension.ExtensionStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_.ImportStatementSupport;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.DeviateStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.DeviationStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.EnumStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ErrorAppTagStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.ErrorMessageStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.FeatureStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.FractionDigitsStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.GroupingStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.IdentityStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.IfFeatureStatementSupport;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta.RefineStatementSupport;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification.NotificationStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification.NotificationStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path.PathStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern.PatternStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule.SubmoduleStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.BaseTypeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.TypeStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type.TypeStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses.SourceGroupingNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses.UsesStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceBehaviours;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * Utility class holding entrypoints for assembling RFC6020/RFC7950 statement {@link CrossSourceStatementReactor}s.
 */
@Beta
public final class RFC7950Reactors {
    private static final StatementSupportBundle INIT_BUNDLE =
        StatementSupportBundle.builder(StatementSupportBundle.VERSIONS_ALL)
            .addSupport(ValidationBundles.BEHAVIOUR)
            .addSupport(NamespaceBehaviours.SUPPORTED_FEATURES)
            .addSupport(NamespaceBehaviours.MODULES_DEVIATED_BY)
            .build();

    private RFC7950Reactors() {
        // Hidden on purpose
    }

    private static StatementSupportBundle stmtDefBundle(final YangParserConfiguration config) {
        return StatementSupportBundle.builderDerivedFrom(INIT_BUNDLE)
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
            .addSupport(new DescriptionStatementSupport(config))
            .addSupport(new ReferenceStatementSupport(config))
            .addSupport(new ContactStatementSupport(config))
            .addSupport(new OrganizationStatementSupport(config))
            .addSupport(NamespaceBehaviours.RESOLVED_INFO)
            .addSupport(NamespaceBehaviours.MODULE)
            .addSupport(NamespaceBehaviours.SUBMODULE)
            .addSupport(new YinElementStatementSupport(config))
            .addSupport(new ArgumentStatementSupport(config))
            .addSupport(new ExtensionStatementSupport(config))
            .addSupport(NamespaceBehaviours.SCHEMA_TREE)
            .addSupport(NamespaceBehaviours.EXTENSION)
            .addSupport(new TypedefStatementSupport(config))
            .addSupport(NamespaceBehaviours.TYPE)
            .addVersionSpecificSupport(VERSION_1, IdentityStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, IdentityStatementSupport.rfc7950Instance(config))
            .addSupport(NamespaceBehaviours.IDENTITY)
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
            .addSupport(StatementDefinitions.BEHAVIOUR)
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
        final StatementSupportBundle stmtDefBundle = stmtDefBundle(config);
        final StatementSupportBundle fullDeclarationBundle =
            fullDeclarationBundle(stmtDefBundle, xpathSupport, config);

        return new CustomCrossSourceStatementReactorBuilder(StatementSupportBundle.VERSIONS_ALL)
                .addAllSupports(ModelProcessingPhase.INIT, INIT_BUNDLE)
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
        return StatementSupportBundle.builderDerivedFrom(stmtDefBundle)
            .addSupport(new LeafStatementSupport(config))
            .addSupport(new ConfigStatementSupport(config))
            .addSupport(new DeviationStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1, DeviateStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, DeviateStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, ChoiceStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, ChoiceStatementSupport.rfc7950Instance(config))
            .addVersionSpecificSupport(VERSION_1, CaseStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, CaseStatementSupport.rfc7950Instance(config))
            .addSupport(new MustStatementSupport(xpathSupport, config))
            .addSupport(new MandatoryStatementSupport(config))
            .addSupport(new AnyxmlStatementSupport(config))
            .addVersionSpecificSupport(VERSION_1_1, new AnydataStatementSupport(config))
            .addSupport(NamespaceBehaviours.FEATURE)
            .addVersionSpecificSupport(VERSION_1, IfFeatureStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, IfFeatureStatementSupport.rfc7950Instance(config))
            .addSupport(NamespaceBehaviours.GROUPING)
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
            .addVersionSpecificSupport(VERSION_1, RefineStatementSupport.rfc6020Instance(config))
            .addVersionSpecificSupport(VERSION_1_1, RefineStatementSupport.rfc7950Instance(config))
            .addSupport(new FeatureStatementSupport(config))
            .addSupport(new PositionStatementSupport(config))
            .addSupport(new ValueStatementSupport(config))
            .addSupport(YangNamespaceContextNamespace.BEHAVIOUR)
            .build();
    }
}
