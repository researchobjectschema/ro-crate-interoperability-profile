package ch.eth.sis.rocrate.facade;

import edu.kit.datamanager.ro_crate.entities.data.DataEntity;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IMetadataEntry
{

    /**
     * Returns the ID of this entry
     */
    String getId();

    /* Returns the types of the entry */
    Set<String> getTypes();

    /* These are key-value pairs for serialization. These are single-valued.
     * Serializable classes are: String, Number and Boolean */
    Map<String, Serializable> getValues();

    /* These are references to other objects in the graph.
     * Each key may have one or more references */
    Map<String, List<String>> getReferences();

    /**
     * Returns a path associated with the file if possible. This might be a directory or a File.
     * Directories can be consumed as the user wishes.
     *
     * @return
     */
    Optional<Path> getFileOrDirectory();

    List<DataEntity> getDataEntitiesReferenced();

}
