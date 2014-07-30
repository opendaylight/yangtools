package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BindingEventStreamProducer {

    public <T extends DataObject> void writeTo(final InstanceIdentifier<T> path, final T data,final BindingStreamEventWriter writer ) {

        getStreamProducer(data.getImplementedInterface());
    }

    private void getStreamProducer(final Class<? extends DataContainer> implementedInterface) {
        // TODO Auto-generated method stub

    }
}
