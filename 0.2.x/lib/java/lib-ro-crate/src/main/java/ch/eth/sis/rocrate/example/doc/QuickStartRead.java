package ch.eth.sis.rocrate.example.doc;

import ch.eth.sis.rocrate.SchemaFacade;
import ch.eth.sis.rocrate.facade.IMetadataEntry;
import ch.eth.sis.rocrate.facade.IPropertyType;
import ch.eth.sis.rocrate.facade.IType;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.FolderReader;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;

import java.util.List;

public class QuickStartRead
{

    public static void main(String[] args) throws JsonProcessingException
    {
        RoCrateReader reader = new RoCrateReader(new FolderReader());
        RoCrate crate = reader.readCrate(QuickStartWrite.TMP_EXAMPLE_CRATE);
        SchemaFacade schemaFacade = SchemaFacade.of(crate);

        List<IType> types = schemaFacade.getTypes();


        /* Writes out all types with their entries */
        for (IType type : types)
        {
            System.out.println(type);
            for (IMetadataEntry entry : schemaFacade.getEntries(type.getId()))
            {
                System.out.println(entry);
            }
        }
        /* Writes out all property types */
        for (IPropertyType propertyType : schemaFacade.getPropertyTypes())
        {
            System.out.println(propertyType);
        }

    }
}
