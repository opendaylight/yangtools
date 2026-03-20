/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.NetconfConstants;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.UndeclaredStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quirk integrating RFC6241 and RFC6536. The goal is to not allow {@code delete-config} and {@code kill-session}
 * by default. The conditions are that:
 * <ol>
 *   <li>we are in fact in {@code module ietf-netconf} with effective {@code revision 2011-06-01}</li>
 *   <li>there is either a {@code rpc delete-config} or {@code rpc kill-session} defined in that module</li>
 *   <li>there is at least one {@code ietf-netconf-acm} module in the reactor, which
 *     <ol>
 *       <li>defines a {@code default-deny-all} extension</li>
 *       <li>that extension is supported by the parser</li>
 *       <li>that extension's support provides the ability to create undeclared statements</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p>If all of these are met, we will create an undeclared {@link EffectiveDefaultDenyWriteStatement} in each of
 * the found RPCs, effectively achieving the intent of {@code ietf-netconf@2013-09-19.yang}.
 */
final class Quirk20130929 implements InferenceAction {
    private static final Logger LOG = LoggerFactory.getLogger(Quirk20130929.class);
    private static final String DEFAULT_DENY_ALL = DefaultDenyAllStatement.DEF.statementName().getLocalName();

    private final @NonNull ArrayList<Prerequisite<? extends StmtContext<?, ?, ?>>> reqNacmModules;
    private final @NonNull Prerequisite<? extends Mutable<?, ?, ?>> reqIetfNetconf;

    private Quirk20130929(final Prerequisite<? extends Mutable<?, ?, ?>> reqIetfNetconf,
            final List<? extends Prerequisite<? extends StmtContext<?, ?, ?>>> reqNacmModules) {
        this.reqIetfNetconf = requireNonNull(reqIetfNetconf);
        this.reqNacmModules = new ArrayList<>(reqNacmModules);
    }

    static void applyTo(final @NonNull Mutable<?, ?, GetFilterElementAttributesEffectiveStatement> gfeaStmt) {
        final var root = gfeaStmt.getRoot();
        final var rootModule = root.asDeclaring(ModuleStatement.DEF);
        if (rootModule == null) {
            LOG.debug("Ignoring quirk in non-module {}", root);
            return;
        }
        final var module = root.currentModule();
        if (!module.equals(NetconfConstants.RFC6241_MODULE)) {
            LOG.debug("Ignoring quirk in non-RFC6241 module {}", module);
            return;
        }

        final var nacmModules = verifyNotNull(root.namespace(ParserNamespaces.NAMESPACE_TO_MODULE)).entrySet().stream()
            .filter(entry -> {
                final var stmt = entry.getValue();
                if (!NACMConstants.MODULE_NAME.equals(stmt.argument())) {
                    LOG.trace("Skipping mis-named {}", stmt);
                    return false;
                }
                if (!NACMConstants.MODULE_NAMESPACE.equals(entry.getKey().namespace())) {
                    LOG.trace("Skipping mis-namespaced {}", stmt);
                    return false;
                }
                LOG.trace("Accepting {}", stmt);
                return true;
            })
            // latest revisions first
            .sorted((e1, e2) -> e2.getKey().revisionUnion().compareTo(e1.getKey().revisionUnion()))
            .map(Entry::getValue)
            .collect(Collectors.toUnmodifiableList());
        if (nacmModules.isEmpty()) {
            LOG.debug("Ignoring quirk as there are no ietf-netconf-acm modules present");
            return;
        }

        LOG.trace("Considering {}", nacmModules);

        // At this point we need to look at the completely-declared module, so we need to hook an inference action.
        final var inference = root.newInferenceAction(ModelProcessingPhase.FULL_DECLARATION);
        inference.apply(new Quirk20130929(inference.mutatesCtx(rootModule, ModelProcessingPhase.FULL_DECLARATION),
            nacmModules.stream()
                .map(nacmModule -> inference.requiresCtx(nacmModule, ModelProcessingPhase.FULL_DECLARATION))
                .toList()));
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final var ietfNetconf = reqIetfNetconf.resolve(ctx);

        final var rpcs = new ArrayList<Mutable<QName, RpcStatement, RpcEffectiveStatement>>();
        for (var stmt : ietfNetconf.mutableDeclaredSubstatements()) {
            final var rpc = stmt.asDeclaring(RpcStatement.DEF);
            if (rpc != null && rpc.isSupportedToBuildEffective()) {
                switch (rpc.getArgument().getLocalName()) {
                    case "delete-config", "kill-session" -> rpcs.add(rpc);
                    default -> {
                        // no-op
                    }
                }
            }
        }
        if (rpcs.isEmpty()) {
            LOG.debug("Ignoring quirk as there are no target RPCs");
            return;
        }

        for (var reqNacmModule : reqNacmModules) {
            final var nacmModule = reqNacmModule.resolve(ctx);
            for (var stmt : nacmModule.declaredSubstatements()) {
                final var extension = stmt.asDeclaring(ExtensionStatement.DEF);
                if (extension == null) {
                    LOG.trace("Skipping non-extension {}", stmt);
                    continue;
                }
                final var qname = extension.getArgument();
                if (!DEFAULT_DENY_ALL.equals(qname.getLocalName())) {
                    LOG.trace("Skipping extension {}", qname);
                    continue;
                }
                if (!extension.isSupportedToBuildEffective()) {
                    LOG.debug("Skipping unsupported extension {}", qname);
                    continue;
                }

                final var support = ietfNetconf.namespaceItem(StatementSupport.NAMESPACE, qname);
                if (support == null) {
                    LOG.debug("Skipping effective extension {} without support, weird", qname);
                    continue;
                }
                if (!(support instanceof UndeclaredStatementFactory)) {
                    LOG.debug("Skipping effective extension {} without support for undeclared statements", qname);
                    continue;
                }

                for (var rpc : rpcs) {
                    final var defaultDenyAll = rpc.createUndeclaredSubstatement(support, null);
                    rpc.addEffectiveSubstatement(defaultDenyAll);
                    LOG.debug("Quirk added {} to rpc {}", defaultDenyAll.publicDefinition(),
                        rpc.getArgument().getLocalName());
                }
                return;
            }
        }
        LOG.debug("Skipping quirk as there is no supported nacm:default-deny-all implementation");
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        reqNacmModules.removeAll(failed);
    }
}
