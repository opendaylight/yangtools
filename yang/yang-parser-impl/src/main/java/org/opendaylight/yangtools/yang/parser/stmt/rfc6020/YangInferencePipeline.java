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

import org.opendaylight.yangtools.yang.parser.spi.source.ModuleQNameToModuleName;

import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public final class YangInferencePipeline {

    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle.builder()
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
            .addSupport(global(SubmoduleNamespace.class))
            .addSupport(global(NamespaceToModule.class))
            .addSupport(global(ModuleNameToModuleQName.class))
            .addSupport(global(ModuleQNameToModuleName.class))
            .addSupport(global(PrefixToModule.class))
            .addSupport(global(ModuleIdentifierToModuleQName.class))
            .addSupport(sourceLocal(ImportedModuleContext.class))
            .addSupport(sourceLocal(IncludedModuleContext.class))
            .addSupport(sourceLocal(ImpPrefixToModuleIdentifier.class))
            .addSupport(sourceLocal(BelongsToPrefixToModuleName.class))
            //.addSupport(global(ImpPrefixToModuleIdentifier.class))
                    .build();

    private static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle.
            derivedFrom(LINKAGE_BUNDLE)
            .addSupport(new YinElementStatementImpl.Definition())
            .addSupport(new ArgumentStatementImpl.Definition())
            .addSupport(new ExtensionStatementImpl.Definition())
            //TODO: implement extension support in SourceSpecificContext
            // in order to prepare statements for full declaration phase,
            // when those ones are read.
            .build();

    private static final StatementSupportBundle FULL_DECL_BUNDLE = StatementSupportBundle.
            derivedFrom(STMT_DEF_BUNDLE)
            .addSupport(new ContainerStatementImpl.Definition())
            .addSupport(new LeafStatementImpl.Definition())
            .addSupport(new TypeStatementImpl.Definition())
            .addSupport(new ConfigStatementImpl.Definition())
            .addSupport(new DeviationStatementImpl.Definition())
            .addSupport(new DeviateStatementImpl.Definition())
            .addSupport(new ChoiceStatementImpl.Definition())
            .addSupport(new CaseStatementImpl.Definition())
            .addSupport(new DefaultStatementImpl.Definition())
            .addSupport(new MustStatementImpl.Definition())
            .addSupport(new MandatoryStatementImpl.Definition())
            .addSupport(new TypedefStatementImpl.Definition())
            .addSupport(new AnyxmlStatementImpl.Definition())
            .addSupport(new IfFeatureStatementImpl.Definition())
            .addSupport(new UsesStatementImpl.Definition())
            .addSupport(new GroupingStatementImpl.Definition())
            .addSupport(treeScoped(GroupingNamespace.class)) //treeScoped
            .addSupport(new StatusStatementImpl.Definition())
            .addSupport(new ErrorMessageStatementImpl.Definition())
            .addSupport(new ErrorAppTagStatementImpl.Definition())
            .addSupport(new LeafListStatementImpl.Definition())
            .addSupport(new ListStatementImpl.Definition())
            .addSupport(new PresenceStatementImpl.Definition())
            .addSupport(new KeyStatementImpl.Definition())
            .addSupport(new MaxElementsStatementImpl.Definition())
            .addSupport(new MinElementsStatementImpl.Definition())
            .addSupport(new OrderedByStatementImpl.Definition())
            .addSupport(new WhenStatementImpl.Definition())
            .addSupport(new AugmentStatementImpl.Definition())
            .addSupport(new RefineStatementImpl.Definition())
            .addSupport(new IdentityStatementImpl.Definition())
            .addSupport(new BaseStatementImpl.Definition())
            .addSupport(new FractionDigitsStatementImpl.Definition())
            .addSupport(new EnumStatementImpl.Definition())
            .addSupport(new FeatureStatementImpl.Definition())
            .addSupport(new RpcStatementImpl.Definition())
            .addSupport(new InputStatementImpl.Definition())
            .addSupport(new OutputStatementImpl.Definition())
            .addSupport(new LengthStatementImpl.Definition())
            .addSupport(new NotificationStatementImpl.Definition())
            .addSupport(new PatternStatementImpl.Definition())
            .addSupport(new PositionStatementImpl.Definition())
            .addSupport(new RangeStatementImpl.Definition())
            .addSupport(new ValueStatementImpl.Definition())
            .addSupport(new UnitsStatementImpl.Definition())
            .addSupport(new RequireInstanceStatementImpl.Definition())
            //TODO: add mapping to Rfc6020Mapping class and uncomment following. Please test it.
//            .addSupport(new EnumSpecificationImpl.Definition())
//            .addSupport(new Decimal64SpecificationImpl.Definition())
//            .addSupport(new IdentityRefSpecificationImpl.Definition())
//            .addSupport(new InstanceIdentifierSpecificationImpl.Definition())
//            .addSupport(new LeafrefSpecificationImpl.Definition())
//            .addSupport(new NumericalRestrictionsImpl.Definition())
//            .addSupport(new StringRestrictionsImpl.Definition())
//            .addSupport(new UnionSpecificationImpl.Definition())
//            .addSupport(new BitStatementImpl.Definition())
            .build();

    public static final Map<ModelProcessingPhase, StatementSupportBundle> RFC6020_BUNDLES = ImmutableMap
            .<ModelProcessingPhase, StatementSupportBundle> builder()
            .put(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.STATEMENT_DEFINITION,STMT_DEF_BUNDLE)
            .put(ModelProcessingPhase.FULL_DECLARATION,FULL_DECL_BUNDLE)
            .put(ModelProcessingPhase.EFFECTIVE_MODEL,FULL_DECL_BUNDLE)
            .build();

    public static final CrossSourceStatementReactor RFC6020_REACTOR = CrossSourceStatementReactor.builder()
            .setBundle(ModelProcessingPhase.SOURCE_LINKAGE, LINKAGE_BUNDLE)
            .setBundle(ModelProcessingPhase.STATEMENT_DEFINITION,STMT_DEF_BUNDLE)
            .setBundle(ModelProcessingPhase.FULL_DECLARATION,FULL_DECL_BUNDLE)
            .setBundle(ModelProcessingPhase.EFFECTIVE_MODEL,FULL_DECL_BUNDLE)
            .build();

    private YangInferencePipeline() {
        throw new UnsupportedOperationException("Utility class");
    }
}
