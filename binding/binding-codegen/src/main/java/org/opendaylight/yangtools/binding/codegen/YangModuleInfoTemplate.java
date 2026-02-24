/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QNameModule;
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

    private final @NonNull Function<ModuleLike, Optional<String>> moduleFilePathResolver;
    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull Module module;
    private final @NonNull String packageName;
    private final @NonNull String modelBindingProviderName;
    private final boolean hasYangData;

    private String importedTypes = CORE_IMPORT_STR;

    public YangModuleInfoTemplate(final Module module, final EffectiveModelContext modelContext,
            final Function<ModuleLike, Optional<String>> moduleFilePathResolver) {
        this.module = requireNonNull(module);
        this.modelContext = requireNonNull(modelContext);
        this.moduleFilePathResolver = requireNonNull(moduleFilePathResolver);
        packageName = Naming.getServicePackageName(module.getQNameModule());
        modelBindingProviderName = packageName + '.' + Naming.MODEL_BINDING_PROVIDER_CLASS_NAME;
        hasYangData = module.asEffectiveStatement().findFirstEffectiveSubstatement(YangDataEffectiveStatement.class)
            .isPresent();
    }

    @NonNull String modelBindingProviderName() {
        return modelBindingProviderName;
    }

    @NonNull String packageName() {
        return packageName;
    }

    public String generate() {
        final var submodules = new LinkedHashSet<Submodule>();
        collectSubmodules(submodules, module);

        var sb = new StringBuilder()
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
            // FIXME: pass down indent
            .append(classBody(module, Naming.MODULE_INFO_CLASS_NAME, submodules)).append("\n")
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
                """)
            .append("    public static @NonNull QName ").append(Naming.MODULE_INFO_QNAMEOF_METHOD_NAME)
                .append("(final String localName) {\n")
            .append("        return QName.create(NAME, localName).intern();\n")
            .append("    }\n");

        if (hasYangData) {
            sb
                .append("""

                    /**
                     * Create an interned {@link YangDataName} with specified {@code templateName} and \
                namespace/revision of
                     * this module.
                     *
                     * @param templateName template name
                     * @return A YangDataName
                     * @throws NullPointerException if {@code templateName} is {@code null}
                     * @throws IllegalArgumentException if {@code templateName} is empty
                     */
                 """)
                .append("    public static @NonNull YangDataName ")
                    .append(Naming.MODULE_INFO_YANGDATANAMEOF_METHOD_NAME).append("(final String templateName) {\n")
                .append("        return new YangDataName(NAME.getModule(), templateName).intern();\n")
                .append("    }\n");
        }
        final var body = sb.append("}\n").toString();

        final var sb2 = new StringBuilder()
            .append("package ").append(packageName).append(";\n")
            .append('\n')
            .append(importedTypes);
        if (hasYangData) {
            sb2.append("import org.opendaylight.yangtools.yang.common.YangDataName;\n");
        }
        return sb2
            .append('\n')
            .append(body)
            .toString();
    }

    public String generateModelProvider() {
        return "package " + packageName + ";\n"
            +  """

                import java.lang.Override;
                import java.util.ServiceLoader;
                import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
                import org.opendaylight.yangtools.binding.meta.YangModuleInfo;

                /**
                """
            +  " * The {@link YangModelBindingProvider} for {@code " + module.getName()
                + "} module. This class should not be used\n"
            +  " * directly, but rather through {@link ServiceLoader}.\n"
            +  " */\n"
            +  '@' + JavaFileTemplate.GENERATED + "(\"mdsal-binding-generator\")\n"
            +  "public final class " + Naming.MODEL_BINDING_PROVIDER_CLASS_NAME
                + " implements YangModelBindingProvider {\n"
            +  """
                    /**
                     * Construct a new provider.
                     */
                """
            +  "    public " + Naming.MODEL_BINDING_PROVIDER_CLASS_NAME + "() {\n"
            +  "        // Nothing else\n"
            +  "    }\n"
            +  '\n'
            +  "    @Override\n"
            +  "    public YangModuleInfo getModuleInfo() {\n"
            +  "        return " + Naming.MODULE_INFO_CLASS_NAME + ".INSTANCE;\n"
            +  "    }\n"
            +  "}\n";
    }

    private static void collectSubmodules(final LinkedHashSet<Submodule> dest, final ModuleLike module) {
        for (var submodule : module.getSubmodules()) {
            if (dest.add(submodule)) {
                collectSubmodules(dest, submodule);
            }
        }
    }

    @NonNullByDefault
    private String classBody(final ModuleLike mod, final String className, final Set<Submodule> submodules) {
        final var sb = new StringBuilder()
            .append("private ").append(className).append("() {\n");

        if (!mod.getImports().isEmpty() || !submodules.isEmpty()) {
            importedTypes = YangModuleInfoTemplate.EXT_IMPORT_STR;
            sb.append("    Set<YangModuleInfo> set = new HashSet<>();\n");
        }

        for (var imp : mod.getImports()) {
            final var name = imp.getModuleName().getLocalName();
            final var optRrev = imp.getRevision();

            final QNameModule qnameModule;
            if (optRrev.isEmpty()) {
                final var sorted = new TreeMap<RevisionUnion, Module>();
                for (var m : modelContext.getModules()) {
                    if (name.equals(m.getName())) {
                        sorted.put(m.getQNameModule().revisionUnion(), m);
                    }
                }
                qnameModule = sorted.lastEntry().getValue().getQNameModule();
            } else {
                qnameModule = modelContext.findModule(name, optRrev).orElseThrow().getQNameModule();
            }

            sb.append("    set.add(").append(Naming.getServicePackageName(qnameModule)).append('.')
                .append(Naming.MODULE_INFO_CLASS_NAME).append(".INSTANCE);\n");
        }

        for (var submodule : submodules) {
            sb.append("    set.add(").append(Naming.getClassName(submodule.getName())).append("Info.INSTANCE);\n");
        }

        if (mod.getImports().isEmpty() && submodules.isEmpty()) {
            sb.append("    importedModules = ImmutableSet.of();\n");
        } else {
            sb.append("    importedModules = ImmutableSet.copyOf(set);\n");
        }

        final var sourcePath = moduleFilePathResolver.apply(mod)
            .orElseThrow(() -> new IllegalStateException("Module " + mod + " does not have a file path"));

        sb
            .append("""
                }

                @Override
                public QName getName() {
                    return NAME;
                }

                @Override
                protected String resourceName() {
                """)
            .append("    return \"").append(sourcePath).append("\";\n")
            .append("""
                }

                @Override
                public ImmutableSet<YangModuleInfo> getImportedModules() {
                    return importedModules;
                }
                """);

        for (var sub : submodules) {
            final var subName = Naming.getClassName(sub.getName());

            sb
                .append('\n')
                .append("private static final class ").append(subName)
                    .append("Info extends ResourceYangModuleInfo {\n")
                .append("    private final @NonNull QName NAME = QName.create(\"")
                    .append(sub.getQNameModule().namespace()).append("\", ");
            sub.getRevision().ifPresent(rev -> sb.append("\"").append(rev).append("\", "));
            sb
                .append('"').append(sub.getName()).append("\").intern();\n")
                .append('\n')
                .append("    static final @NonNull YangModuleInfo INSTANCE = new ").append(subName)
                    .append("Info();\n")
                .append('\n')
                .append("    private final @NonNull ImmutableSet<YangModuleInfo> importedModules;\n")
                .append('\n')
                .append(classBody(sub, subName + "Info", Set.of()))
                .append("}\n");
        }

        final var sc = new StringConcatenation();
        sc.append("    ");
        sc.append(sb, "    ");
        return sc.toString();
    }
}
