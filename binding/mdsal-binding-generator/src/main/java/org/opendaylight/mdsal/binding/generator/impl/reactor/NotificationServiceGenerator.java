/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * Aggregate service for top-level {@code notification} statements for a particular module. It does not handle nested
 * (YANG 1.1) notifications.
 */
// FIXME: MDSAL-497: remove this generator
final class NotificationServiceGenerator extends AbstractImplicitGenerator {
    private final List<NotificationGenerator> notifs;

    NotificationServiceGenerator(final ModuleGenerator parent, final List<NotificationGenerator> notifs) {
        super(parent);
        this.notifs = requireNonNull(notifs);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.NOTIFICATION_LISTENER;
    }

    @Override
    String classSuffix() {
        return Naming.NOTIFICATION_LISTENER_SUFFIX;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.NOTIFICATION_LISTENER)
            .addAnnotation(DEPRECATED_ANNOTATION);

        for (NotificationGenerator gen : notifs) {
            final MethodSignatureBuilder notificationMethod = builder.addMethod("on" + gen.assignedName())
                .setAccessModifier(AccessModifier.PUBLIC)
                .addParameter(gen.getGeneratedType(builderFactory), "notification")
                .setReturnType(Types.primitiveVoidType());

            final NotificationEffectiveStatement stmt = gen.statement();
            verify(stmt instanceof WithStatus, "Unexpected statement %s", stmt);
            final WithStatus withStatus = (WithStatus) stmt;

            annotateDeprecatedIfNecessary(withStatus, notificationMethod);
            if (withStatus.getStatus() == Status.OBSOLETE) {
                notificationMethod.setDefault(true);
            }

            // FIXME: finish this up
            // addComment(notificationMethod, notification);
        }

        return builder.build();
    }
}
