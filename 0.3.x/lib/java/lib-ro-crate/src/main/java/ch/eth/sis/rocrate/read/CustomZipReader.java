package ch.eth.sis.rocrate.read;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.reader.ReaderStrategy;
import edu.kit.datamanager.ro_crate.reader.ZipReader;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

final class CustomZipReader implements ReaderStrategy, IRootFolderExtractor
{
    private Path rootFolder = null;
    private final String ID = UUID.randomUUID().toString();
    private final Path temporaryFolder = Path.of(String.format("./.tmp/ro-crate-java/zipReader/%s/", ID));
    private boolean isExtracted = false;


    public CustomZipReader() {
        super();
        ZipReader z;
    }

    public String getID() {
        return ID;
    }

    public Path getTemporaryFolder() {
        return temporaryFolder;
    }

    public boolean isExtracted() {
        return isExtracted;
    }

    private void readCrate(String location) {
        try {
            File folder = temporaryFolder.toFile();
            // ensure the directory is clean
            if (folder.isDirectory()) {
                FileUtils.cleanDirectory(folder);
            } else if (folder.isFile()) {
                FileUtils.delete(folder);
            }
            // extract
            try (ZipFile zf = new ZipFile(location)) {
                zf.extractAll(temporaryFolder.toAbsolutePath().toString());
                this.isExtracted = true;
            }
            // register deletion on exit
            FileUtils.forceDeleteOnExit(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public ObjectNode readMetadataJson(String location) {
        if (!isExtracted) {
            this.readCrate(location);
        }

        ObjectMapper objectMapper = MyObjectMapper.getMapper();

        try {
            Path root = getRootFolderLocation(location);
            File jsonMetadata = root.resolve("ro-crate-metadata.json").toFile();
            return objectMapper.readTree(jsonMetadata).deepCopy();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public File readContent(String location) {
        if (!isExtracted) {
            this.readCrate(location);
        }
        return getRootFolderLocation(location).toFile();
    }

    @Override
    public Path getRootFolderLocation(String location)
    {
        if(!isExtracted) {
            readCrate(location);
        }
        if(rootFolder == null) {
            rootFolder = ReadHelper.resolveSingleRootDir(temporaryFolder.toFile());
        }
        return rootFolder;
    }
}
