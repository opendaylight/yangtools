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
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedIdentitiesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleQNameToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StmtOrderingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.Builder;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.AbstractBuiltInTypeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BooleanEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EmptyEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int8EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt8EffectiveStatementImpl;

public final class YangInferencePipeline {

    public static final StatementSupportBundle INIT_BUNDLE = StatementSupportBundle
            .builder().addSupport(global(ValidationBundlesNamespace.class))
            .build();

    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle
            .derivedFrom(INIT_BUNDLE)
            .addSupport(new ModuleStatementSupport())
            .addSupport(new SubmoduleStatementImpl.Definition())
            .addSupport(new NamespaceStatementImpl.Definition())
            .addSupport(new ImportStatementDefinition())
            .addSupport(new IncludeStatementImpl.Definition())
            .addSupport(new PrefixStatementImpl.Definition())
            .addSupport(new YangVersionStatementImpl.Definition())
            .addSupport(new DescriptionStatementImpl.Definition())
            .addSupport(new RevisionStatementImpl.Definition())
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
            .addSupport(global(ModuleCtxToModuleQName.class))
            .addSupport(global(ModuleQNameToModuleName.class))
            .addSupport(global(PrefixToModule.class))
            .addSupport(global(ModuleIdentifierToModuleQName.class))
            .addSupport(sourceLocal(ImportedModuleContext.class))
            .addSupport(sourceLocal(IncludedModuleContext.class))
            .addSupport(sourceLocal(IncludedSubmoduleNameToIdentifier.class))
            .addSupport(sourceLocal(ImpPrefixToModuleIdentifier.class))
            .addSupport(sourceLocal(BelongsToModuleContext.class))
            .addSupport(sourceLocal(QNameToStatementDefinition.class))
            .addSupport(sourceLocal(BelongsToPrefixToModuleName.class)).build();


    private static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle
            .derivedFrom(LINKAGE_BUNDLE)
            .addSupport(new YinElementStatementImpl.Definition())
            .addSupport(new ArgumentStatementImpl.Definition())
            .addSupport(new ExtensionStatementImpl.Definition())
            .addSupport(global(ChildSchemaNodes.class))
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
            .addSupport(new PatternStatementImpl.Definition())
            .addSupport(new RangeStatementImpl.Definition())
            .addSupport(new ContainerStatementImpl.Definition())
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
            .build();

    private static final StatementSupportBundle FULL_DECL_BUNDLE = StatementSupportBundle
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
            .addSupport(new RefineStatementImpl.Definition())
            .addSupport(new FeatureStatementImpl.Definition())
            .addSupport(new PositionStatementImpl.Definition())
            .addSupport(new ValueStatementImpl.Definition())
            .addSupport(global(StmtOrderingNamespace.class))
            .build();

    public static final Map<ModelProcessingPhase, StatementSupportBundle> RFC6020_BUNDLES = ImmutableMap
            .<ModelProcessingPhase, StatementSupportBundle> builder()
            .put(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.STATEMENT_DEFINITION, STMT_DEF_BUNDLE)
            .put(ModelProcessingPhase.FULL_DECLARATION, FULL_DECL_BUNDLE)
            .put(ModelProcessingPhase.EFFECTIVE_MODEL, FULL_DECL_BUNDLE)
            .build();

    private static final Set<AbstractBuiltInTypeEffectiveStatement<?>> BUILTIN_TYPES;
    static {
        final ImmutableSet.Builder<AbstractBuiltInTypeEffectiveStatement<?>> b = ImmutableSet.builder();
        b.add(Int8EffectiveStatementImpl.getInstance());
        b.add(Int16EffectiveStatementImpl.getInstance());
        b.add(Int32EffectiveStatementImpl.getInstance());
        b.add(Int64EffectiveStatementImpl.getInstance());
        b.add(UInt8EffectiveStatementImpl.getInstance());
        b.add(UInt16EffectiveStatementImpl.getInstance());
        b.add(UInt32EffectiveStatementImpl.getInstance());
        b.add(UInt64EffectiveStatementImpl.getInstance());
        b.add(StringEffectiveStatementImpl.getInstance());
        b.add(BooleanEffectiveStatementImpl.getInstance());
        b.add(EmptyEffectiveStatementImpl.getInstance());
        b.add(BinaryEffectiveStatementImpl.getInstance());

        BUILTIN_TYPES = b.build();
    }

    public static final CrossSourceStatementReactor RFC6020_REACTOR;
    static {
        Builder b = CrossSourceStatementReactor.builder();
        b.setBundle(ModelProcessingPhase.INIT, INIT_BUNDLE);
        b.setBundle(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE);
        b.setBundle(ModelProcessingPhase.STATEMENT_DEFINITION, STMT_DEF_BUNDLE);
        b.setBundle(ModelProcessingPhase.FULL_DECLARATION, FULL_DECL_BUNDLE);
        b.setBundle(ModelProcessingPhase.EFFECTIVE_MODEL, FULL_DECL_BUNDLE);
        b.setValidationBundle(ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS,
            YangValidationBundles.SUPPORTED_REFINE_SUBSTATEMENTS);
        b.setValidationBundle(ValidationBundleType.SUPPORTED_AUGMENT_TARGETS,
            YangValidationBundles.SUPPORTED_AUGMENT_TARGETS);
        b.setValidationBundle(ValidationBundleType.SUPPORTED_CASE_SHORTHANDS,
            YangValidationBundles.SUPPORTED_CASE_SHORTHANDS);
        b.setValidationBundle(ValidationBundleType.SUPPORTED_DATA_NODES,
            YangValidationBundles.SUPPORTED_DATA_NODES);

        for (AbstractBuiltInTypeEffectiveStatement<?> s : BUILTIN_TYPES) {
            b.addStatementToNamespace(TypeNamespace.class, s.getQName(), s);
        }

        RFC6020_REACTOR = b.build();
    }


    private YangInferencePipeline() {
        throw new UnsupportedOperationException("Utility class");
    }
}
