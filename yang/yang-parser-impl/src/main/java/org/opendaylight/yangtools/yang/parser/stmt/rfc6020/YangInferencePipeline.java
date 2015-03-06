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

import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;

import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class YangInferencePipeline {


    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle.builder()
            .addSupport(new ModuleStatementSupport())
            .addSupport(new NamespaceStatementImpl.Definition())
            .addSupport(new ImportStatementDefinition())
            .addSupport(new PrefixStatementImpl.Definition())
            .addSupport(global(ModuleNamespace.class))
            .addSupport(global(NamespaceToModule.class))
            .addSupport(sourceLocal(ImportedModuleContext.class))
            .build();

    private static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle.
            derivedFrom(LINKAGE_BUNDLE)
            .build();

    private static final StatementSupportBundle FULL_DECL_BUNDLE = StatementSupportBundle.
            derivedFrom(STMT_DEF_BUNDLE)
            .addSupport(new ContainerStatementImpl.Definition())
            .addSupport(sourceLocal(PrefixToModule.class))
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

}
