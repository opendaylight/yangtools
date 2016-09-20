/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.Builder;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class CustomStatementParserBuilder {
    private final Map<ModelProcessingPhase, StatementSupportBundle.Builder> reactorSupportBundles = ImmutableMap
            .<ModelProcessingPhase, StatementSupportBundle.Builder> builder()
            .put(ModelProcessingPhase.INIT, StatementSupportBundle.builder())
            .put(ModelProcessingPhase.SOURCE_PRE_LINKAGE, StatementSupportBundle.builder())
            .put(ModelProcessingPhase.SOURCE_LINKAGE, StatementSupportBundle.builder())
            .put(ModelProcessingPhase.STATEMENT_DEFINITION, StatementSupportBundle.builder())
            .put(ModelProcessingPhase.FULL_DECLARATION, StatementSupportBundle.builder())
            .put(ModelProcessingPhase.EFFECTIVE_MODEL, StatementSupportBundle.builder()).build();
    private final Map<ValidationBundleType, Collection<StatementDefinition>> reactorValidationBundles = new HashMap<>();

    public CustomStatementParserBuilder addStatementSupport(final ModelProcessingPhase phase,
            final StatementSupport<?, ?, ?> stmtSupport) {
        reactorSupportBundles.get(phase).addSupport(stmtSupport);
        return this;
    }

    public CustomStatementParserBuilder addNamespaceSupport(final ModelProcessingPhase phase,
            final NamespaceBehaviour<?, ?, ?> namespaceSupport) {
        reactorSupportBundles.get(phase).addSupport(namespaceSupport);
        return this;
    }

    public CustomStatementParserBuilder addDefaultRFC6020Bundles() {
        addRFC6020SupportBundles();
        addRFC6020ValidationBundles();
        return this;
    }

    private void addRFC6020ValidationBundles() {
        reactorValidationBundles.putAll(YangInferencePipeline.RFC6020_VALIDATION_BUNDLE);
    }

    private void addRFC6020SupportBundles() {
        for (final Entry<ModelProcessingPhase, StatementSupportBundle> entry : YangInferencePipeline.RFC6020_BUNDLES
                .entrySet()) {
            addAllSupports(entry.getKey(), entry.getValue());
        }
    }

    public CustomStatementParserBuilder addValidationBundle(final ValidationBundleType validationBundleType,
            final Collection<StatementDefinition> validationBundle) {
        reactorValidationBundles.put(validationBundleType, validationBundle);
        return this;
    }

    public CustomStatementParserBuilder addAllSupports(final ModelProcessingPhase phase,
            final StatementSupportBundle stmtSupportBundle) {
        addAllStatementSupports(phase, stmtSupportBundle.getDefinitions().values());
        addAllNamespaceSupports(phase, stmtSupportBundle.getNamespaceDefinitions().values());
        return this;
    }

    public CustomStatementParserBuilder addAllNamespaceSupports(final ModelProcessingPhase phase,
            final Collection<NamespaceBehaviour<?, ?, ?>> namespaceSupports) {
        final StatementSupportBundle.Builder stmtBundleBuilder = reactorSupportBundles.get(phase);
        for (final NamespaceBehaviour<?, ?, ?> namespaceSupport : namespaceSupports) {
            stmtBundleBuilder.addSupport(namespaceSupport);
        }
        return this;
    }

    public CustomStatementParserBuilder addAllStatementSupports(final ModelProcessingPhase phase,
            final Collection<StatementSupport<?, ?, ?>> statementSupports) {
        final StatementSupportBundle.Builder stmtBundleBuilder = reactorSupportBundles.get(phase);
        for (final StatementSupport<?, ?, ?> statementSupport : statementSupports) {
            stmtBundleBuilder.addSupport(statementSupport);
        }
        return this;
    }

    public CrossSourceStatementReactor build() {
        final StatementSupportBundle initBundle = reactorSupportBundles.get(ModelProcessingPhase.INIT).build();
        final StatementSupportBundle preLinkageBundle = reactorSupportBundles
                .get(ModelProcessingPhase.SOURCE_PRE_LINKAGE).setParent(initBundle).build();
        final StatementSupportBundle linkageBundle = reactorSupportBundles.get(ModelProcessingPhase.SOURCE_LINKAGE)
                .setParent(preLinkageBundle).build();
        final StatementSupportBundle stmtDefBundle = reactorSupportBundles
                .get(ModelProcessingPhase.STATEMENT_DEFINITION).setParent(linkageBundle).build();
        final StatementSupportBundle fullDeclBundle = reactorSupportBundles.get(ModelProcessingPhase.FULL_DECLARATION)
                .setParent(stmtDefBundle).build();
        final StatementSupportBundle effectiveBundle = reactorSupportBundles.get(ModelProcessingPhase.EFFECTIVE_MODEL)
                .setParent(fullDeclBundle).build();

        final Builder reactorBuilder = CrossSourceStatementReactor.builder()
                .setBundle(ModelProcessingPhase.INIT, initBundle)
                .setBundle(ModelProcessingPhase.SOURCE_PRE_LINKAGE, preLinkageBundle)
                .setBundle(ModelProcessingPhase.SOURCE_LINKAGE, linkageBundle)
                .setBundle(ModelProcessingPhase.STATEMENT_DEFINITION, stmtDefBundle)
                .setBundle(ModelProcessingPhase.FULL_DECLARATION, fullDeclBundle)
                .setBundle(ModelProcessingPhase.EFFECTIVE_MODEL, effectiveBundle);

        for (final Entry<ValidationBundleType, Collection<StatementDefinition>> entry : reactorValidationBundles
                .entrySet()) {
            reactorBuilder.setValidationBundle(entry.getKey(), entry.getValue());
        }

        return reactorBuilder.build();
    }
}
