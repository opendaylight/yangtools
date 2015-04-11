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
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public final class YangInferencePipeline {

    public static final StatementSupportBundle LINKAGE_BUNDLE = StatementSupportBundle.builder()
            .addSupport(new ModuleStatementSupport())
            .addSupport(new NamespaceStatementImpl.Definition())
            .addSupport(new ImportStatementDefinition())
            .addSupport(new PrefixStatementImpl.Definition())
            .addSupport(global(ModuleNamespace.class))
            .addSupport(global(NamespaceToModule.class))
            .addSupport(sourceLocal(ImportedModuleContext.class))
            .build();

    private static final StatementSupportBundle STMT_DEF_BUNDLE = StatementSupportBundle.derivedFrom(LINKAGE_BUNDLE).build();

    private static final StatementSupportBundle FULL_DECL_BUNDLE = StatementSupportBundle.derivedFrom(STMT_DEF_BUNDLE).build();

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
