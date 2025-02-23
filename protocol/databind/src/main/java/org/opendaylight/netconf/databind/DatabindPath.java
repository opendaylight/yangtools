/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

/**
 * A request path (such as the path part of a RESTCONF request URI} resolved against a {@link DatabindContext}. This can
 * be either
 * <ul>
 *   <li>a {@link Data} pointing to a datastore resource, or</li>
 *   <li>an {@link Rpc} pointing to a YANG {@code rpc} statement, or</li>
 *   <li>an {@link Action} pointing to an instantiation of a YANG {@code action} statement</li>
 * </ul>
 */
@NonNullByDefault
public sealed interface DatabindPath {
    /**
     * Returns the associated DatabindContext.
     *
     * @return the associated DatabindContext
     */
    DatabindContext databind();

    /**
     * Returns the {@link EffectiveStatementInference} made by this path.
     *
     * @return the {@link EffectiveStatementInference} made by this path
     */
    Inference inference();

    /**
     * A {@link DatabindPath} denoting an invocation of a YANG {@code action}.
     *
     * @param databind the {@link DatabindContext} to which this path is bound
     * @param inference the {@link EffectiveStatementInference} made by this path
     * @param instance the {@link YangInstanceIdentifier} of the instance being referenced, guaranteed to be
     *        non-empty
     * @param action the {@code action}
     */
    record Action(
            DatabindContext databind,
            Inference inference,
            YangInstanceIdentifier instance,
            ActionEffectiveStatement action) implements OperationPath, InstanceReference {
        public Action {
            requireNonNull(inference);
            requireNonNull(action);
            if (instance.isEmpty()) {
                throw new IllegalArgumentException("action must be instantiated on a data resource");
            }
        }

        @Override
        public InputEffectiveStatement inputStatement() {
            return action.input();
        }

        @Override
        public OutputEffectiveStatement outputStatement() {
            return action.output();
        }
    }

    /**
     * A {@link DatabindPath} denoting a datastore instance.
     *
     * @param databind the {@link DatabindContext} to which this path is bound
     * @param inference the {@link EffectiveStatementInference} made by this path
     * @param instance the {@link YangInstanceIdentifier} of the instance being referenced,
     *                 {@link YangInstanceIdentifier#empty()} denotes the datastore
     * @param schema the {@link DataSchemaContext} of the datastore instance
     */
    // FIXME: split into 'Datastore' and 'Data' with non-empty instance, so we can bind to correct
    //        instance-identifier semantics, which does not allow YangInstanceIdentifier.empty()
    record Data(
            DatabindContext databind,
            Inference inference,
            YangInstanceIdentifier instance,
            DataSchemaContext schema) implements InstanceReference {
        public Data {
            requireNonNull(inference);
            requireNonNull(instance);
            requireNonNull(schema);
        }

        // FIXME: this is the 'Datastore' constructor
        public Data(final DatabindContext databind) {
            this(databind, Inference.ofDataTreePath(databind.modelContext()), YangInstanceIdentifier.of(),
                databind.schemaTree().getRoot());
        }
    }

    /**
     * A {@link DatabindPath} denoting an invocation of a YANG {@code rpc}.
     *
     * @param databind the {@link DatabindContext} to which this path is bound
     * @param inference the {@link EffectiveStatementInference} made by this path
     * @param rpc the {@code rpc}
     */
    record Rpc(DatabindContext databind, Inference inference, RpcEffectiveStatement rpc) implements OperationPath {
        public Rpc {
            requireNonNull(inference);
            requireNonNull(rpc);
        }

        @Override
        public InputEffectiveStatement inputStatement() {
            return rpc.input();
        }

        @Override
        public OutputEffectiveStatement outputStatement() {
            return rpc.output();
        }
    }

    /**
     * An intermediate trait of {@link DatabindPath}s which are referencing a YANG data resource. This can be either
     * a {@link Data}, or an {@link Action}}.
     */
    sealed interface InstanceReference extends DatabindPath {
        /**
         * Returns the {@link YangInstanceIdentifier} of the instance being referenced.
         *
         * @return the {@link YangInstanceIdentifier} of the instance being referenced,
         *         {@link YangInstanceIdentifier#empty()} denotes the data root
         */
        YangInstanceIdentifier instance();

        /**
         * Returns this reference as a {@link ErrorPath}.
         *
         * @return this reference as a {@link ErrorPath}
         */
        default ErrorPath toErrorPath() {
            return new ErrorPath(databind(), instance());
        }
    }

    /**
     * An intermediate trait of {@link DatabindPath}s which are referencing a YANG operation. This can be either
     * an {@link Action}, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-4.4.2">RFC8040 Invoke Operation Mode</a> or
     * an {@link Rpc}, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-3.6">RFC8040 Operation Resource</a>.
     */
    sealed interface OperationPath extends DatabindPath {
        /**
         * Returns the {@code input} statement of this operation.
         *
         * @return the {@code input} statement of this operation
         */
        InputEffectiveStatement inputStatement();

        /**
         * Returns the {@code output} statement of this operation.
         *
         * @return the {@code output} statement of this operation
         */
        OutputEffectiveStatement outputStatement();
    }
}
