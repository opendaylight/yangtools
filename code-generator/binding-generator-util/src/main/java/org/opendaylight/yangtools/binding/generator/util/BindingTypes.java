package org.opendaylight.yangtools.binding.generator.util;

import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.*;
import static org.opendaylight.yangtools.binding.generator.util.Types.*;

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

    private BindingTypes() {

    }

    public static final ParameterizedType augmentable(Type t) {
        return parameterizedTypeFor(AUGMENTABLE, t);
    }
}
