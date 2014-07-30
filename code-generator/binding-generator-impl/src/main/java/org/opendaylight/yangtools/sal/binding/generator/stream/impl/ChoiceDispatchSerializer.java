package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;

public class ChoiceDispatchSerializer implements DataObjectSerializerImplementation {

    @Override
    public void serialize(final DataObjectSerializerRegistry reg, final DataObject obj, final BindingStreamEventWriter stream) {
        @SuppressWarnings("rawtypes")
        Class cazeClass = obj.getImplementedInterface();
        reg.getSerializer(cazeClass).serialize(obj, stream);
    }
}
