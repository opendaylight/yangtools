package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChoiceDispatchSerializer implements DataObjectSerializerImplementation {

    private static final Logger LOG = LoggerFactory.getLogger(ChoiceDispatchSerializer.class);

    @SuppressWarnings("rawtypes")
    private final Class choiceClass;

    @SuppressWarnings("rawtypes")
    private ChoiceDispatchSerializer(final Class choiceClass) {
        super();
        this.choiceClass = choiceClass;
    }

    static final ChoiceDispatchSerializer from(final Class<? extends DataContainer> choiceClass) {
        return new ChoiceDispatchSerializer(choiceClass);
    }

    static final ChoiceDispatchSerializer from(final Type choiceClass) throws IllegalStateException {
        try {
        return new ChoiceDispatchSerializer(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy().loadClass(
                choiceClass));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(final DataObjectSerializerRegistry reg, final DataObject obj, final BindingStreamEventWriter stream) {
        @SuppressWarnings("rawtypes")
        Class cazeClass = obj.getImplementedInterface();
        stream.startChoiceNode(choiceClass, BindingStreamEventWriter.UNKNOWN_SIZE);
        DataObjectSerializer caseSerializer = reg.getSerializer(cazeClass);
        if(caseSerializer != null) {
            caseSerializer.serialize(obj, stream);
        } else {
            LOG.warn("No serializer for case {} is available in registry {}",cazeClass,reg);
        }
        stream.endNode();
    }
}
