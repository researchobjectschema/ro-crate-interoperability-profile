package ch.eth.sis.rocrate.read;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper;
import edu.kit.datamanager.ro_crate.reader.ReaderStrategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

final class CustomFolderReader implements ReaderStrategy, IRootFolderExtractor
{

    private Path rootFolder = null;

    @Override
    public ObjectNode readMetadataJson(String location) {
        ObjectMapper objectMapper = MyObjectMapper.getMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        try {
            Path properRoot = getRootFolderLocation(location);
            Path metadata = properRoot.resolve("ro-crate-metadata.json");
            objectNode = objectMapper.readTree(metadata.toFile()).deepCopy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return objectNode;
    }

    @Override
    public File readContent(String location) {
        return getRootFolderLocation(location).toFile();
    }

    @Override
    public Path getRootFolderLocation(String location)
    {
        if(rootFolder == null) {
            rootFolder = ReadHelper.resolveSingleRootDir(new File(location));
        }
        return rootFolder;
    }
}
