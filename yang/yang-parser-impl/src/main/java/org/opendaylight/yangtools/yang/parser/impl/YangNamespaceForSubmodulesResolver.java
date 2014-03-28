package org.opendaylight.yangtools.yang.parser.impl;

import static org.opendaylight.yangtools.yang.parser.util.ParserListenerUtils.stringFromNode;

import java.net.URI;

import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_header_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Namespace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yang_version_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParserBaseListener;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangNamespaceForSubmodulesResolver extends YangParserBaseListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(YangParserListenerImpl.class);

    private final String sourcePath;
    private ModuleBuilder moduleBuilder;
    private String moduleName;
    private URI namespace;

    public YangNamespaceForSubmodulesResolver(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public void enterModule_stmt(YangParser.Module_stmtContext ctx) {
        moduleName = stringFromNode(ctx);
        LOGGER.debug("entering module " + moduleName);
        enterLog("module", moduleName, 0);

        moduleBuilder = new ModuleBuilder(moduleName, sourcePath);
    }

    @Override
    public void enterModule_header_stmts(Module_header_stmtsContext ctx) {
        enterLog("module_header", "", ctx.getStart().getLine());
        String yangVersion = null;
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Namespace_stmtContext) {
                final String namespaceStr = stringFromNode(treeNode);
                namespace = URI.create(namespaceStr);
                moduleBuilder.setNamespace(namespace);
                setLog("namespace", namespaceStr);
            } else if (treeNode instanceof Yang_version_stmtContext) {
                yangVersion = stringFromNode(treeNode);
                setLog("yang-version", yangVersion);
            }
        }

        if (yangVersion == null) {
            yangVersion = "1";
        }
        moduleBuilder.setYangVersion(yangVersion);
    }

    @Override
    public void enterBelongs_to_stmt(YangParser.Belongs_to_stmtContext ctx) {
        moduleBuilder.setBelongsTo(stringFromNode(ctx));
    }

    @Override
    public void enterSubmodule_stmt(YangParser.Submodule_stmtContext ctx) {
        moduleName = stringFromNode(ctx);

        LOGGER.debug("entering submodule " + moduleName);
        enterLog("submodule", moduleName, 0);

        moduleBuilder = new ModuleBuilder(moduleName, true, sourcePath);
    }

    public ModuleBuilder getModuleBuilder() {
        return moduleBuilder;
    }

    private void enterLog(String p1, String p2, int line) {
        LOGGER.trace("entering {} {} ({})", p1, p2, line);
    }

    private void setLog(String p1, String p2) {
        LOGGER.trace("setting {} {}", p1, p2);
    }
}
