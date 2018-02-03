package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkArgument;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.PrefixToEffectiveModuleNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.QNameModuleToPrefixNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;

public abstract class YinInputFactory {
    private static final class Default extends YinInputFactory {
        private static final XMLEventFactory XEF = XMLEventFactory.newFactory();

        @Override
        protected XMLEventReader createXMLEventReader(final ModuleEffectiveStatement module,
                final ModuleStatement statement) {
            return new YinXMLEventReader(XEF, statement, module.getAll(PrefixToEffectiveModuleNamespace.class),
                module.getAll(QNameModuleToPrefixNamespace.class));
        }
    }

    private static final YinInputFactory DEFAULT = new Default();

    protected YinInputFactory() {

    }

    public static YinInputFactory defaultInstance() {
        return DEFAULT;
    }

    public final XMLEventReader createXMLEventReader(final ModuleEffectiveStatement module) {
        final ModuleStatement declared = module.getDeclared();
        checkArgument(declared != null);
        return createXMLEventReader(module, declared);
    }

    protected abstract XMLEventReader createXMLEventReader(ModuleEffectiveStatement module, ModuleStatement statement);
}
