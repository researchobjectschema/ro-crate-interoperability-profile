package ch.eth.sis.rocrate.read;

import ch.eth.sis.rocrate.SchemaFacade;
import ch.eth.sis.rocrate.facade.IMetadataEntry;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import junit.framework.TestCase;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RoCrateLoaderTest extends TestCase
{

    private static final String RESOURCE_DIRECTORY =
            "src/test/resources/ch/eth/sis/rocrate/read/";

    private static final String FOLDER = RESOURCE_DIRECTORY + "space/root/";
    private static final String FOLDER_WITH_ROOT = RESOURCE_DIRECTORY + "space/";
    private static final String ZIP = RESOURCE_DIRECTORY + "space.zip";
    private static final String ZIP_WITH_ROOT = RESOURCE_DIRECTORY + "space_with_root.zip";

    public void testCustomFolderRead_withRootFolder() throws Exception
    {
        RoCrate crate = RoCrateLoader.read(FOLDER_WITH_ROOT);
        SchemaFacade schema = SchemaFacade.of(crate);

        assertSchema(schema);
    }

    public void testCustomFolderRead_withoutRootFolder() throws Exception
    {
        RoCrate crate = RoCrateLoader.read(FOLDER);
        SchemaFacade schema = SchemaFacade.of(crate);

        assertSchema(schema);
    }

    public void testCustomZipRead_withRootFolder() throws Exception
    {
        RoCrate crate = RoCrateLoader.read(ZIP_WITH_ROOT);
        SchemaFacade schema = SchemaFacade.of(crate);

        assertSchema(schema);
    }

    public void testCustomZipRead_withoutRootFolder() throws Exception
    {
        RoCrate crate = RoCrateLoader.read(ZIP);
        SchemaFacade schema = SchemaFacade.of(crate);

        assertSchema(schema);
    }

    private void assertSchema(SchemaFacade schemaFacade) {
        assertEquals(8, schemaFacade.getTypes().size());
        assertEquals(25, schemaFacade.getPropertyTypes().size());
        assertEquals(5, schemaFacade.getAllEntries().size());
        assertEquals(1, schemaFacade.getVocabularyTypes().size());
        Set<String> ids = schemaFacade.getAllEntries().stream().map(IMetadataEntry::getId).collect(Collectors.toSet());

        Set<String> expectedIds = Set.of("SCICAT", "/SCICAT/PROJECT", "/SCICAT/PROJECT/PROJECT_EXP_1", "/SCICAT/PROJECT/ENTRY64", "/SCICAT/PROJECT/ENTRY120");
        assertEquals(expectedIds, ids);

        assertEquals(6, schemaFacade.getEntry("/SCICAT/PROJECT/ENTRY120").getDataEntitiesReferenced().size());
        Set<String> referencedFilesEntry120 = schemaFacade.getEntry("/SCICAT/PROJECT/ENTRY120").getDataEntitiesReferenced().stream().map(
                DataEntity::getPath).filter(Objects::nonNull).map(Path::toString).collect(Collectors.toSet());
        Set<String> shortPaths = stripPathPrefix(referencedFilesEntry120);

        Set<String> expectedFiles = Set.of(
               "/hierarchy/SCICAT/PROJECT/MY_EXPERIMENT (PROJECT_EXP_1)/TEST_ENTRY2 (ENTRY120)/data/test_entry2_data2.txt",
               "/hierarchy/SCICAT/PROJECT/MY_EXPERIMENT (PROJECT_EXP_1)/TEST_ENTRY2 (ENTRY120)/data/test_entry2_data1.txt",
               "/xlsx/miscellaneous/file-service/eln-lims/97/f6/ec/97f6ec9b-e0ff-4ca0-8e3d-0739c14f3140/5e90a9f1-6968-4b49-8b06-4204fb90748f.png"
        );
        assertEquals(expectedFiles, shortPaths);


        assertEquals(6, schemaFacade.getEntry("/SCICAT/PROJECT/ENTRY64").getDataEntitiesReferenced().size());
        Set<String> referencedFilesEntry64 = schemaFacade.getEntry("/SCICAT/PROJECT/ENTRY64").getDataEntitiesReferenced().stream().map(
                DataEntity::getPath).filter(Objects::nonNull).map(Path::toString).collect(Collectors.toSet());
        shortPaths = stripPathPrefix(referencedFilesEntry64);

        expectedFiles = Set.of(
                "/xlsx/miscellaneous/file-service/eln-lims/dc/fc/74/dcfc74f2-59fa-4a7d-a062-2751b854787a/25e2ea54-cd6c-423c-9321-3ed5e65f62d2.png",
                "/xlsx/miscellaneous/file-service/eln-lims/fe/28/35/fe28357e-a410-4cf9-9c43-fc5bb7d7d40e/56a6425a-2f59-47b5-bacb-e1b03828c5a5.png",
                "/hierarchy/SCICAT/PROJECT/MY_EXPERIMENT (PROJECT_EXP_1)/TEST_ENTRY (ENTRY64)/data/test_entry_data.txt"
        );
        assertEquals(expectedFiles, shortPaths);
    }

    private Set<String> stripPathPrefix(Set<String> files) {
        Set<String> shortPaths = new HashSet<>();
        for(String path : files) {
            if(path.contains("/hierarchy/")) {
                shortPaths.add(path.substring(path.indexOf("/hierarchy/")));
            } else if(path.contains("/xlsx/")) {
                shortPaths.add(path.substring(path.indexOf("/xlsx/")));
            } else {
                shortPaths.add(path);
            }
        }
        return shortPaths;
    }


}
