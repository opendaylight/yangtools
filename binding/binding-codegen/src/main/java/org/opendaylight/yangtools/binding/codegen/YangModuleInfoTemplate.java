/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.Submodule;

/**
 * Template for {@link YangModuleInfo} implementation for a particular module. Aside from fulfilling that contract,
 * this class provides a static {@code createQName(String)} method, which is used by co-generated code to initialize
 * QNAME constants.
 */
@SuppressWarnings("all")
public final class YangModuleInfoTemplate {
    // These are always imported. Note we need to import even java.lang members, as there can be conflicting definitions
    // in our package
    private static final String CORE_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private static final String EXT_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import java.util.HashSet;
        import java.util.Set;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private final Function<ModuleLike, Optional<String>> moduleFilePathResolver;
    private final EffectiveModelContext modelContext;
    private final Module module;
    private final boolean hasYangData;
    private final String packageName;
    private final String modelBindingProviderName;

    private String importedTypes = CORE_IMPORT_STR;

    public YangModuleInfoTemplate(final Module module, final EffectiveModelContext modelContext,
            final Function<ModuleLike, Optional<String>> moduleFilePathResolver) {
        this.module = requireNonNull(module);
        this.modelContext = requireNonNull(modelContext);
        this.moduleFilePathResolver = moduleFilePathResolver;
        packageName = Naming.getServicePackageName(module.getQNameModule());
        modelBindingProviderName = packageName + '.' + Naming.MODEL_BINDING_PROVIDER_CLASS_NAME;
        hasYangData = module.asEffectiveStatement().findFirstEffectiveSubstatement(YangDataEffectiveStatement.class)
            .isPresent();
    }

    public String generate() {
        final var submodules = new HashSet<Submodule>();
        collectSubmodules(submodules, module);

        final var sb = new StringBuilder()
            .append("/**\n")
            .append(" * The {@link ResourceYangModuleInfo} for {@code ").append(module.getName()).append("} module.\n")
            .append(" */\n")
            .append('@').append(JavaFileTemplate.GENERATED).append("(\"mdsal-binding-generator\")\n")
            .append("public final class ").append(Naming.MODULE_INFO_CLASS_NAME)
                .append(" extends ResourceYangModuleInfo {\n")
            .append("    private static final @NonNull QName NAME = QName.create(\"")
                .append(module.getQNameModule().namespace()).append("\", ");
        module.getRevision().ifPresent(revision -> sb.append('"').append(revision).append("\", "));
        sb
            .append('"').append(module.getName()).append("\").intern();\n")
            .append('\n')
            .append("    /**\n")
            .append("     * The singleton instance.\n")
            .append("     */\n")
            .append("    public static final @NonNull YangModuleInfo INSTANCE = new ")
                .append(Naming.MODULE_INFO_CLASS_NAME).append("();\n")
            .append('\n')
            .append("    private final @NonNull ImmutableSet<YangModuleInfo> importedModules;\n")
            .append('\n')
            // FIXME: inline
            .append("    ").append(classBody(module, Naming.MODULE_INFO_CLASS_NAME, submodules)).append("\n\n")
            .append("""
                /**
                 * Create an interned {@link QName} with specified {@code localName} and namespace/revision of this
                 * module.
                 *
                 * @param localName local name
                 * @return A QName
                 * @throws NullPointerException if {@code localName} is {@code null}
                 * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier
                 */
                """);

        final var _builder = new StringConcatenation();
        _builder.append(sb.toString());

        //                public static @NonNull QName «MODULE_INFO_QNAMEOF_METHOD_NAME»(final String localName) {
        //                    return QName.create(NAME, localName).intern();
        //                }
        _builder.append("    ");
        _builder.append("public static @NonNull QName ");
        _builder.append(Naming.MODULE_INFO_QNAMEOF_METHOD_NAME, "    ");
        _builder.append("(final String localName) {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("return QName.create(NAME, localName).intern();");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();

//            «IF hasYangData»
//
//                /**
//                 * Create an interned {@link YangDataName} with specified {@code templateName} and namespace/revision of
//                 * this module.
//                 *
//                 * @param templateName template name
//                 * @return A YangDataName
//                 * @throws NullPointerException if {@code templateName} is {@code null}
//                 * @throws IllegalArgumentException if {@code templateName} is empty
//                 */
//                public static @NonNull YangDataName «MODULE_INFO_YANGDATANAMEOF_METHOD_NAME»(final String templateName) {
//                    return new YangDataName(NAME.getModule(), templateName).intern();
//                }
//            «ENDIF»
//            }
        if (hasYangData) {
            _builder.newLine();
            _builder.append("/**");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* Create an interned {@link YangDataName} with specified {@code templateName} and namespace/revision of");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* this module.");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("*");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* @param templateName template name");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* @return A YangDataName");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* @throws NullPointerException if {@code templateName} is {@code null}");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* @throws IllegalArgumentException if {@code templateName} is empty");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("*/");
            _builder.newLine();
            _builder.append("public static @NonNull YangDataName ");
            _builder.append(Naming.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME);
            _builder.append("(final String templateName) {");
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.append("return new YangDataName(NAME.getModule(), templateName).intern();");
            _builder.newLine();
            _builder.append("}");
            _builder.newLine();
        }
        _builder.append("}");
        _builder.newLine();
        final var body = _builder.toString();

//        return '''
//            package «packageName»;
//
//            «importedTypes»
//            «IF hasYangData»
//            import org.opendaylight.yangtools.yang.common.YangDataName;
//            «ENDIF»
//
//            «body»
//        '''.toString
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("package ");
        _builder_1.append(packageName);
        _builder_1.append(";");
        _builder_1.newLineIfNotEmpty();
        _builder_1.newLine();
        _builder_1.append(importedTypes);
        _builder_1.newLineIfNotEmpty();
        if (hasYangData) {
            _builder_1.append("import org.opendaylight.yangtools.yang.common.YangDataName;");
            _builder_1.newLine();
        }
        _builder_1.newLine();
        _builder_1.append(body);
        _builder_1.newLineIfNotEmpty();
        return _builder_1.toString();
    }

    public String generateModelProvider() {
//        package «packageName»;
//
//        import java.lang.Override;
//        import java.util.ServiceLoader;
//        import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
//        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
//
//        /**
//         * The {@link YangModelBindingProvider} for {@code «module.name»} module. This class should not be used
//         * directly, but rather through {@link ServiceLoader}.
//         */
//        @«JavaFileTemplate.GENERATED»("mdsal-binding-generator")
//        public final class «MODEL_BINDING_PROVIDER_CLASS_NAME» implements YangModelBindingProvider {
//            /**
//             * Construct a new provider.
//             */
//            public «MODEL_BINDING_PROVIDER_CLASS_NAME»() {
//                // Nothing else
//            }
//
//            @Override
//            public YangModuleInfo getModuleInfo() {
//                return «MODULE_INFO_CLASS_NAME».INSTANCE;
//            }
//        }
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("package ");
        _builder.append(packageName);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("import java.lang.Override;");
        _builder.newLine();
        _builder.append("import java.util.ServiceLoader;");
        _builder.newLine();
        _builder.append("import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;");
        _builder.newLine();
        _builder.append("import org.opendaylight.yangtools.binding.meta.YangModuleInfo;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("/**");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* The {@link YangModelBindingProvider} for {@code ");
        _builder.append(module.getName(), " ");
        _builder.append("} module. This class should not be used");
        _builder.newLineIfNotEmpty();
        _builder.append(" ");
        _builder.append("* directly, but rather through {@link ServiceLoader}.");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("@");
        _builder.append(JavaFileTemplate.GENERATED);
        _builder.append("(\"mdsal-binding-generator\")");
        _builder.newLineIfNotEmpty();
        _builder.append("public final class ");
        _builder.append(Naming.MODEL_BINDING_PROVIDER_CLASS_NAME);
        _builder.append(" implements YangModelBindingProvider {");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("/**");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("* Construct a new provider.");
        _builder.newLine();
        _builder.append("     ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("public ");
        _builder.append(Naming.MODEL_BINDING_PROVIDER_CLASS_NAME, "    ");
        _builder.append("() {");
        _builder.newLineIfNotEmpty();
        _builder.append("        ");
        _builder.append("// Nothing else");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("public YangModuleInfo getModuleInfo() {");
        _builder.newLine();
        _builder.append("        ");
        _builder.append("return ");
        _builder.append(Naming.MODULE_INFO_CLASS_NAME, "        ");
        _builder.append(".INSTANCE;");
        _builder.newLineIfNotEmpty();
        _builder.append("    ");
        _builder.append("}");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        return _builder.toString();
    }

    private static void collectSubmodules(final HashSet<Submodule> dest, final ModuleLike module) {
        for (var submodule : module.getSubmodules()) {
            if (dest.add(submodule)) {
                YangModuleInfoTemplate.collectSubmodules(dest, submodule);
            }
        }
    }

    private CharSequence classBody(final ModuleLike m, final String className, final Set<Submodule> submodules) {
//        private «className»() {
//            «IF !m.imports.empty || !submodules.empty»
//                «extendImports»
//                Set<YangModuleInfo> set = new HashSet<>();
//            «ENDIF»
//            «IF !m.imports.empty»
//                «FOR imp : m.imports»
//                    «val name = imp.moduleName.localName»
//                    «val rev = imp.revision»
//                    «IF rev.empty»
//                        «val sorted = new TreeMap<RevisionUnion, Module>()»
//                        «FOR module : ctx.modules»
//                            «IF name.equals(module.name)»
//                                «sorted.put(module.QNameModule.revisionUnion, module)»
//                            «ENDIF»
//                        «ENDFOR»
//                        set.add(«sorted.lastEntry().value.QNameModule.getServicePackageName».«MODULE_INFO_CLASS_NAME».INSTANCE);
//                    «ELSE»
//                        set.add(«(ctx.findModule(name, rev).orElseThrow.QNameModule).getServicePackageName».«MODULE_INFO_CLASS_NAME».INSTANCE);
//                    «ENDIF»
//                «ENDFOR»
//            «ENDIF»
//            «FOR submodule : submodules»
//                set.add(«submodule.name.className»Info.INSTANCE);
//            «ENDFOR»
//            «IF m.imports.empty && submodules.empty»
//                importedModules = ImmutableSet.of();
//            «ELSE»
//                importedModules = ImmutableSet.copyOf(set);
//            «ENDIF»
//        }
//
//        @Override
//        public QName getName() {
//            return NAME;
//        }
//
//        @Override
//        protected String resourceName() {
//            return "«sourcePath(m)»";
//        }
//
//        @Override
//        public ImmutableSet<YangModuleInfo> getImportedModules() {
//            return importedModules;
//        }
//        «generateSubInfo(submodules)»
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("private ");
        _builder.append(className);
        _builder.append("() {");
        _builder.newLineIfNotEmpty();
        if (!m.getImports().isEmpty() || !submodules.isEmpty()) {
            _builder.append("    ");
            extendImports();
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.append("Set<YangModuleInfo> set = new HashSet<>();");
            _builder.newLine();
        }

        final var imports = m.getImports();
        if (!imports.isEmpty()) {
            for (var imp : m.getImports()) {
                _builder.append("    ");
                final String name = imp.getModuleName().getLocalName();
                _builder.newLineIfNotEmpty();
                _builder.append("    ");
                final var optRrev = imp.getRevision();
                _builder.newLineIfNotEmpty();
                if (optRrev.isEmpty()) {
                    _builder.append("    ");
                    final var sorted = new TreeMap<RevisionUnion, Module>();
                    _builder.newLineIfNotEmpty();
                    for (var module : modelContext.getModules()) {
                        if (name.equals(module.getName())) {
                            _builder.append("    ");
                            _builder.append(sorted.put(module.getQNameModule().revisionUnion(), module), "    ");
                            _builder.newLineIfNotEmpty();
                        }
                    }
                    _builder.append("    ");
                    _builder.append("set.add(");
                    _builder.append(Naming.getServicePackageName(sorted.lastEntry().getValue().getQNameModule()), "    ");
                    _builder.append(".");
                    _builder.append(Naming.MODULE_INFO_CLASS_NAME, "    ");
                    _builder.append(".INSTANCE);");
                    _builder.newLineIfNotEmpty();
                } else {
                    _builder.append("    ");
                    _builder.append("set.add(");
                    _builder.append(Naming.getServicePackageName(modelContext.findModule(name, optRrev).orElseThrow().getQNameModule()), "    ");
                    _builder.append(".");
                    _builder.append(Naming.MODULE_INFO_CLASS_NAME, "    ");
                    _builder.append(".INSTANCE);");
                    _builder.newLineIfNotEmpty();
                }
            }
        }
        for (var submodule : submodules) {
            _builder.append("    ");
            _builder.append("set.add(");
            _builder.append(Naming.getClassName(submodule.getName()), "    ");
            _builder.append("Info.INSTANCE);");
            _builder.newLineIfNotEmpty();
        }
        if (m.getImports().isEmpty() && submodules.isEmpty()) {
            _builder.append("    ");
            _builder.append("importedModules = ImmutableSet.of();");
            _builder.newLine();
        } else {
            _builder.append("    ");
            _builder.append("importedModules = ImmutableSet.copyOf(set);");
            _builder.newLine();
        }
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("public QName getName() {");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("return NAME;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("protected String resourceName() {");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("return \"");
        _builder.append(sourcePath(m), "    ");
        _builder.append("\";");
        _builder.newLineIfNotEmpty();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("public ImmutableSet<YangModuleInfo> getImportedModules() {");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("return importedModules;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.append(generateSubInfo(submodules));
        _builder.newLineIfNotEmpty();
        return _builder;
    }

    private void extendImports() {
        importedTypes = YangModuleInfoTemplate.EXT_IMPORT_STR;
    }

    private String sourcePath(final ModuleLike module) {
        return moduleFilePathResolver.apply(module)
            .orElseThrow(() -> new IllegalStateException("Module " + module + " does not have a file path"));
    }

    private CharSequence generateSubInfo(final Set<Submodule> submodules) {
//        «FOR submodule : submodules»
//            «val className = submodule.name.className»
//
//            private static final class «className»Info extends ResourceYangModuleInfo {
//                «val rev = submodule.revision»
//                private final @NonNull QName NAME = QName.create("«submodule.QNameModule.namespace().toString»", «
//                IF rev.present»"«rev.orElseThrow.toString»", «ENDIF»"«submodule.name»").intern();
//
//                static final @NonNull YangModuleInfo INSTANCE = new «className»Info();
//
//                private final @NonNull ImmutableSet<YangModuleInfo> importedModules;
//
//                «classBody(submodule, className + "Info", ImmutableSet.of)»
//            }
//        «ENDFOR»

        StringConcatenation _builder = new StringConcatenation();
        for(var submodule : submodules) {
            final var className = Naming.getClassName(submodule.getName());
            _builder.newLineIfNotEmpty();
            _builder.newLine();
            _builder.append("private static final class ");
            _builder.append(className);
            _builder.append("Info extends ResourceYangModuleInfo {");
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.newLineIfNotEmpty();
            _builder.append("    ");
            _builder.append("private final @NonNull QName NAME = QName.create(\"");
            _builder.append(submodule.getQNameModule().namespace().toString(), "    ");
            _builder.append("\", ");
            final var optRev = submodule.getRevision();
            if (optRev.isPresent()) {
                _builder.append("\"");
                _builder.append(optRev.orElseThrow().toString(), "    ");
                _builder.append("\", ");
            }
            _builder.append("\"");
            _builder.append(submodule.getName(), "    ");
            _builder.append("\").intern();");
            _builder.newLineIfNotEmpty();
            _builder.newLine();
            _builder.append("    ");
            _builder.append("static final @NonNull YangModuleInfo INSTANCE = new ");
            _builder.append(className, "    ");
            _builder.append("Info();");
            _builder.newLineIfNotEmpty();
            _builder.newLine();
            _builder.append("    ");
            _builder.append("private final @NonNull ImmutableSet<YangModuleInfo> importedModules;");
            _builder.newLine();
            _builder.newLine();
            _builder.append("    ");
            _builder.append(classBody(submodule, className + "Info", Set.of()), "    ");
            _builder.newLineIfNotEmpty();
            _builder.append("}");
            _builder.newLine();
        }
        return _builder;
    }
}
