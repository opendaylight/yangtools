/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

/**
 * Aggregate service for top-level {@code rpc} statements for a particular module.
 */
// FIXME: eventually remove this generator
@Deprecated(since = "11.0.0", forRemoval = true)
final class RpcServiceGenerator extends AbstractImplicitGenerator {
    private static final JavaTypeName CHECK_RETURN_VALUE_ANNOTATION =
        // Do not refer to annotation class, as it may not be available at runtime
        JavaTypeName.create("edu.umd.cs.findbugs.annotations", "CheckReturnValue");

    private final List<RpcGenerator> rpcs;

    RpcServiceGenerator(final ModuleGenerator parent, final List<RpcGenerator> rpcs) {
        super(parent);
        this.rpcs = requireNonNull(rpcs);
    }

    @Override
    String classSuffix() {
        return BindingMapping.RPC_SERVICE_SUFFIX;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.RPC_SERVICE);

        for (RpcGenerator rpcGen : rpcs) {
            final RpcEffectiveStatement rpc = rpcGen.statement();
            final QName qname = rpc.argument();

            // FIXME: this may still conflict in theory
            final MethodSignatureBuilder method = builder.addMethod(BindingMapping.getRpcMethodName(qname));

            method.addParameter(getChild(rpcGen, InputEffectiveStatement.class).getGeneratedType(builderFactory),
                "input");
            method.setReturnType(Types.listenableFutureTypeFor(BindingTypes.rpcResult(
                getChild(rpcGen, OutputEffectiveStatement.class).getGeneratedType(builderFactory))));

            // FIXME: this should not be part of runtime types
            method.addAnnotation(CHECK_RETURN_VALUE_ANNOTATION);
            final String rpcName = qname.getLocalName();
            method.setComment(new TypeMemberComment("Invoke {@code " + rpcName + "} RPC.",
                rpc.findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class).orElse(null),
                "@param input of {@code " + rpcName + "}\n"
                    + "@return output of {@code " + rpcName + '}'));
        }

        // FIXME: activate this
        //      addCodegenInformation(interfaceBuilder, module, "RPCs", rpcDefinitions);

        return builder.build();
    }
}
