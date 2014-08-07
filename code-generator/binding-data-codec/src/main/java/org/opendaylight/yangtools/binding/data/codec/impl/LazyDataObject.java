package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.opendaylight.yangtools.binding.data.codec.util.AugmentationSupplier;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

class LazyDataObject implements InvocationHandler, AugmentationSupplier {

    private static final String  GET_IMPLEMENTED_INTERFACE = "getImplementedInterface";
    private static final String  TO_STRING = "toString";

    private static final String  GET_AUGMENTATION = "getAugmentation";
    private final DataObjectCodecContext<?> context;
    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private LazyDataObject(final DataObjectCodecContext<?> ctx, final NormalizedNodeContainer data) {
        this.context = ctx;
        this.data = data;
    }

    @SuppressWarnings("rawtypes")
    static DataObject create(final DataObjectCodecContext ctx, final NormalizedNodeContainer<?,?,?>  data) {
        Class<?> bindingClass = ctx.bindingClass();
        return (DataObject) Proxy.newProxyInstance(bindingClass.getClassLoader(),new Class<?>[]{bindingClass}, new LazyDataObject(ctx,data));
    }


    @Override
    public Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations() {
        return context.getAllAugmentationsFrom(data);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if(method.getParameterTypes().length == 0) {
            String name = method.getName();
            if(GET_IMPLEMENTED_INTERFACE.equals(name)) {
                return context.bindingClass();
            } else if(TO_STRING.equals(name)) {
                return toString();
            }
            // FIXME: Add result object caching
            return context.getBindingChildValue(method, data);
            // FIXME: hascode and equals method
        } else if(GET_AUGMENTATION.equals(method.getName())) {
            return getAugmentationImpl((Class<?>) args[0]);
        }
        return null;
    }

    private Object getAugmentationImpl(final Class<?> cls) {
        DataContainerCodecContext<?> augCtx = context.getStreamChild(cls);
        Optional<NormalizedNode<?, ?>> augData = data.getChild(augCtx.getDomPathArgument());
        if(augData.isPresent()) {
            return augCtx.dataFromNormalizedNode(augData.get());
        }
        return null;
    }

    @Override
    public String toString() {
        return context.bindingClass().getSimpleName() + "Lazy";
    }

}
