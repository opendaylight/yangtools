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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public final class YangInferencePipeline {

    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle.builder()
            .addSupport(new ModuleStatementSupport())
            .addSupport(new SubmoduleStatementImpl.Definition())
            .addSupport(new NamespaceStatementImpl.Definition())
            .addSupport(new ImportStatementDefinition())
            .addSupport(new PrefixStatementImpl.Definition())
            .addSupport(new YangVersionStatementImpl.Definition())
            .addSupport(new DescriptionStatementImpl.Definition())
            .addSupport(new RevisionStatementImpl.Definition())
            .addSupport(new ReferenceStatementImpl.Definition())
            .addSupport(new ContactStatementImpl.Definition())
            .addSupport(new OrganizationStatementImpl.Definition())
            .addSupport(new BelongsToStatementImpl.Definition())
            .addSupport(new IncludeStatementImpl.Definition())
            .addSupport(global(ModuleNamespace.class))
            .addSupport(global(NamespaceToModule.class))
            .addSupport(sourceLocal(ImportedModuleContext.class))
            .addSupport(sourceLocal(BelongsToPrefixToModuleName.class))
                    .build();

    private static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle.
            derivedFrom(LINKAGE_BUNDLE)
            .addSupport(new ExtensionStatementImpl.Definition())
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
            .addSupport(new StatusStatementImpl.Definition())
            .addSupport(new ErrorMessageStatementImpl.Definition())
            .addSupport(new ErrorAppTagStatementImpl.Definition())
            .addSupport(new GroupingStatementImpl.Definition())
            .addSupport(new LeafListStatementImpl.Definition())
            .addSupport(new ListStatementImpl.Definition())
            .addSupport(new PresenceStatementImpl.Definition())
            .addSupport(new KeyStatementImpl.Definition())
            .addSupport(new MaxElementsStatementImpl.Definition())
            .addSupport(new MinElementsStatementImpl.Definition())
            .addSupport(new OrderedByStatementImpl.Definition())
            .addSupport(new WhenStatementImpl.Definition())
            .addSupport(new UsesStatementImpl.Definition())
            .addSupport(new AugmentStatementImpl.Definition())
            .addSupport(new RefineStatementImpl.Definition())
            .build();

    public static final Map<ModelProcessingPhase, StatementSupportBundle> RFC6020_BUNDLES = ImmutableMap
            .<ModelProcessingPhase, StatementSupportBundle> builder()
            .put(ModelProcessingPhase.SourceLinkage, LINKAGE_BUNDLE)
            .put(ModelProcessingPhase.StatementDefinition,STMT_DEF_BUNDLE)
            .put(ModelProcessingPhase.FullDeclaration,FULL_DECL_BUNDLE)
            .put(ModelProcessingPhase.EffectiveModel,FULL_DECL_BUNDLE)
            .build();

    public static final CrossSourceStatementReactor RFC6020_REACTOR = CrossSourceStatementReactor.builder()
            .setBundle(ModelProcessingPhase.SourceLinkage, LINKAGE_BUNDLE)
            .setBundle(ModelProcessingPhase.StatementDefinition,STMT_DEF_BUNDLE)
            .setBundle(ModelProcessingPhase.FullDeclaration,FULL_DECL_BUNDLE)
            .setBundle(ModelProcessingPhase.EffectiveModel,FULL_DECL_BUNDLE)
            .build();

    private YangInferencePipeline() {
        throw new UnsupportedOperationException("Utility class");
    }
}
