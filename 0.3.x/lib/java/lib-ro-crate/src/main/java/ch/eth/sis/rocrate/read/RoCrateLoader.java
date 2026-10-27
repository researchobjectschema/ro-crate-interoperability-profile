package ch.eth.sis.rocrate.read;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;

import java.nio.file.Path;

public class RoCrateLoader
{
    private RoCrateLoader() {}

    public static RoCrate read(String location) {
        if(location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location must not be empty!");
        }
        RoCrateReader roCrateReader;
        Path newLocation;
        if (location.endsWith(".zip") || location.endsWith(".eln"))
        {
            CustomZipReader zipReader = new CustomZipReader();
            newLocation = zipReader.getRootFolderLocation(location);
            roCrateReader = new RoCrateReader(zipReader);
        } else
        {
            CustomFolderReader folderReader = new CustomFolderReader();
            newLocation = folderReader.getRootFolderLocation(location);
            roCrateReader = new RoCrateReader(folderReader);
        }

        return roCrateReader.readCrate(newLocation.toString());
    }
}
