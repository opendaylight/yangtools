/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

/**
 * A request path (such as the path part of a RESTCONF request URI} resolved against a {@link DatabindContext}. This can
 * be either
 * <ul>
 *   <li>a {@link Data} pointing to a datastore resource, or</li>
 *   <li>an {@link Rpc} pointing to a YANG {@code rpc} statement, or</li>
 *   <li>a {@link Notification} pointing to a YANG {@code notification} statement, or</li>
 *   <li>an {@link Action} pointing to an instantiation of a YANG {@code action} statement, or</li>
 *   <li>a {@link DataNodeNotification} pointing to a YANG {@code notification} statement in with a data node, or</li>
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
     * @param statement the {@link ActionEffectiveStatement}
     */
    record Action(
            DatabindContext databind,
            Inference inference,
            YangInstanceIdentifier instance,
            ActionEffectiveStatement statement) implements InstanceRequest, OperationPath {
        public Action {
            requireNonNull(databind);
            requireNonNull(inference);
            requireNonNull(statement);
            if (instance.isEmpty()) {
                throw new IllegalArgumentException("action must be instantiated on a data resource");
            }
        }

        @Override
        public InputEffectiveStatement inputStatement() {
            return statement.input();
        }

        @Override
        public OutputEffectiveStatement outputStatement() {
            return statement.output();
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
            DataSchemaContext schema) implements InstanceRequest {
        public Data {
            requireNonNull(databind);
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
     * A {@link DatabindPath} denoting an invocation of a YANG {@code notification} defined in a data node, introduced
     * in RFC7950.
     *
     * @param databind the {@link DatabindContext} to which this path is bound
     * @param inference the {@link EffectiveStatementInference} made by this path
     * @param instance the {@link YangInstanceIdentifier} of the instance being referenced, guaranteed to be
     *        non-empty
     * @param statement the {@code NotificationEffectiveStatement}
     */
    record DataNodeNotification(
            DatabindContext databind,
            Inference inference,
            YangInstanceIdentifier instance,
            NotificationEffectiveStatement statement) implements DatabindPath, InstanceReference {
        public DataNodeNotification {
            requireNonNull(databind);
            requireNonNull(inference);
            requireNonNull(statement);
            if (instance.isEmpty()) {
                throw new IllegalArgumentException("data node notification must be instantiated on a data resource");
            }
        }
    }

    /**
     * A {@link DatabindPath} denoting an invocation of a YANG {@code notification} defined at the top level
     * of a module, introduced in RFC6020.
     *
     * @param databind the {@link DatabindContext} to which this path is bound
     * @param inference the {@link EffectiveStatementInference} made by this path
     * @param statement the {@code NotificationEffectiveStatement}
     */
    record Notification(
            DatabindContext databind,
            Inference inference,
            NotificationEffectiveStatement statement) implements DatabindPath {
        public Notification {
            requireNonNull(databind);
            requireNonNull(inference);
            requireNonNull(statement);
        }
    }

    /**
     * A {@link DatabindPath} denoting an invocation of a YANG {@code rpc}.
     *
     * @param databind the {@link DatabindContext} to which this path is bound
     * @param inference the {@link EffectiveStatementInference} made by this path
     * @param statement the {@code rpc}
     */
    record Rpc(
            DatabindContext databind,
            Inference inference,
            RpcEffectiveStatement statement) implements OperationPath {
        public Rpc {
            requireNonNull(databind);
            requireNonNull(inference);
            requireNonNull(statement);
        }

        @Override
        public InputEffectiveStatement inputStatement() {
            return statement.input();
        }

        @Override
        public OutputEffectiveStatement outputStatement() {
            return statement.output();
        }
    }

    /**
     * An intermediate trait of {@link DatabindPath}s which are referencing a YANG data resource. This can be either
     * a {@link Data}, an {@link Action}}, or a {@link DataNodeNotification}.
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
     * An intermediate trait of {@link InstanceReference}s which can be requested from a server. This can be either
     * a {@link Data}, or an {@link Action}}.
     */
    @Beta
    sealed interface InstanceRequest extends InstanceReference permits Action, Data {
        // Nothing else
    }

    /**
     * An intermediate trait of {@link DatabindPath}s which are referencing a YANG operation. This can be either
     * an {@link Action}, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-4.4.2">RFC8040 Invoke Operation Mode</a> or
     * an {@link Rpc}, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-3.6">RFC8040 Operation Resource</a>.
     */
    sealed interface OperationPath extends DatabindPath permits Action, Rpc {
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
