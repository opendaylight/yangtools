package org.opendaylight.yangtools.yang.model.util.repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class FilesystemSchemaCachingProvider<I> extends AbstractCachingSchemaSourceProvider<I, InputStream> {

    private final File storageDirectory;
    private final Function<I, String> transformationFunction;

    public FilesystemSchemaCachingProvider(SchemaSourceProvider<I> delegate, File directory,
            Function<I, String> transformationFunction) {
        super(delegate);
        this.storageDirectory = directory;
        this.transformationFunction = transformationFunction;
    }

    @Override
    protected synchronized Optional<InputStream> cacheSchemaSource(String moduleName, Optional<String> revision, Optional<I> source) {
        File schemaFile = toFile(moduleName, revision);
        try {
            if(source.isPresent() && schemaFile.createNewFile()) {
                try (
                        FileOutputStream outStream = new FileOutputStream(schemaFile);
                        OutputStreamWriter writer = new OutputStreamWriter(outStream);
                ) {
                writer.write(transformToString(source.get()));
                writer.flush();
                } catch (IOException e) {
                    
                }
            }
        } catch (IOException e){
            
        }
        return transformToStream(source);
    }

    @SuppressWarnings("deprecation")
    private Optional<InputStream> transformToStream(Optional<I> source) {
        if(source.isPresent()) {
            return Optional.<InputStream>of(new StringBufferInputStream(transformToString(source.get())));
        }
        return Optional.absent();
    }

    private String transformToString(I input) {
        return transformationFunction.apply(input);
    }

    @Override
    protected Optional<InputStream> getCachedSchemaSource(String moduleName, Optional<String> revision) {
        File inputFile = toFile(moduleName, revision);
        try {
            if (inputFile.exists() && inputFile.canRead()) {
                InputStream stream = new FileInputStream(inputFile);
                return Optional.of(stream);
            }
        } catch (FileNotFoundException e) {
            return Optional.absent();
        }
        return Optional.absent();
    }

    private File toFile(String moduleName, Optional<String> revision) {
        return new File(storageDirectory, toYangFileName(moduleName, revision));
    }

    public static final String toYangFileName(String moduleName, Optional<String> revision) {
        StringBuilder filename = new StringBuilder(moduleName);
        if (revision.isPresent()) {
            filename.append("@");
            filename.append(revision.get());
        }
        filename.append(".yang");
        return filename.toString();
    }

    private static final Function<String, String> NOOP_TRANSFORMATION = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return input;
        }
    };

    public static FilesystemSchemaCachingProvider<String> createFromStringSourceProvider(
            SchemaSourceProvider<String> liveProvider, File directory) {
        Preconditions.checkNotNull(liveProvider);
        Preconditions.checkNotNull(directory);
        directory.mkdirs();
        return new FilesystemSchemaCachingProvider<String>(liveProvider, directory,NOOP_TRANSFORMATION);
    }
}
