/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import static org.opendaylight.yangtools.binding.generator.util.Types.parameterizedTypeFor;
import static org.opendaylight.yangtools.binding.generator.util.Types.typeForClass;

import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.*;

public final class BindingTypes {

    public static final ConcreteType AUGMENTABLE = typeForClass(Augmentable.class);
    public static final ConcreteType AUGMENTATION = typeForClass(Augmentation.class);
    public static final ConcreteType BASE_IDENTITY = typeForClass(BaseIdentity.class);
    public static final ConcreteType DATA_OBJECT = typeForClass(DataObject.class);
    public static final ConcreteType DATA_ROOT = typeForClass(DataRoot.class);
    public static final ConcreteType IDENTIFIABLE = typeForClass(Identifiable.class);
    public static final ConcreteType IDENTIFIER = typeForClass(Identifier.class);
    public static final ConcreteType INSTANCE_IDENTIFIER = typeForClass(InstanceIdentifier.class);
    public static final ConcreteType NOTIFICATION = typeForClass(Notification.class);
    public static final ConcreteType NOTIFICATION_LISTENER = typeForClass(NotificationListener.class);
    public static final ConcreteType RPC_SERVICE = typeForClass(RpcService.class);
    public static final ConcreteType YANG_MODELED_ENTITY = typeForClass(YangModeledEntity.class);
    public static final ConcreteType YANG_MODULE_INFO = typeForClass(YangModuleInfo.class);

    private static final ConcreteType CHILD_OF = typeForClass(ChildOf.class);

    private BindingTypes() {
    }

    public static ParameterizedType augmentable(Type t) {
        return parameterizedTypeFor(AUGMENTABLE, t);
    }

    public static ParameterizedType childOf(Type t) {
        return parameterizedTypeFor(CHILD_OF, t);
    }

}
