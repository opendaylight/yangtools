package org.opendaylight.yangtools.yang.model.util.repo;

import java.io.InputStream;

import com.google.common.base.Optional;

public interface SchemaSourceProvider<F> {

    Optional<F> getSchemaSource(String moduleName, Optional<String> revision);

}
