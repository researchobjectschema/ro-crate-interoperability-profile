package ch.eth.sis.rocrate.read;

import java.io.IOException;
import java.nio.file.Path;

interface IRootFolderExtractor
{
    Path getRootFolderLocation(String location) throws IOException;
}
