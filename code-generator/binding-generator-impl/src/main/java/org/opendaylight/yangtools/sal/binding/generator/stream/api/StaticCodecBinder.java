package org.opendaylight.yangtools.sal.binding.generator.stream.api;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public interface StaticCodecBinder {

    StaticCodec getStaticSerializer(Type type);
    StaticCodec getStaticDeserializer(Type type);

    public interface StaticCodec {

    }

    /**
     * No change is required for serialization / deserialization to DOM
     *
     */
    enum NoopCodec implements StaticCodec {
        INSTANCE
    }

    public class StaticMethodCodec implements StaticCodec {

        private final String method;
        private final Type type;

        public StaticMethodCodec(final Type type,final String method) {
            this.method = Preconditions.checkNotNull(method);
            this.type = Preconditions.checkNotNull(type);
        }

        public Type getType() {
            return type;
        }
        public String getMethodName() {
            return method;
        }
    }

}
