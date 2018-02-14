package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

@Beta
@NonNullByDefault
public interface EffectiveModel extends Immutable {

    Optional<List<DeclaredStatement<?>>> getDeclaredStatements();

    Map<QNameModule, ModuleEffectiveStatement> getEffectiveStatements();
}
