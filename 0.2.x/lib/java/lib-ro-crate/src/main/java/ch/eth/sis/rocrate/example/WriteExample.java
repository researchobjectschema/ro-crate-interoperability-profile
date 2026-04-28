package ch.eth.sis.rocrate.example;

import ch.eth.sis.rocrate.SchemaFacade;
import ch.eth.sis.rocrate.facade.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class WriteExample
{

    /**
     * This is a small example that adds one type for a  textual resource with two properties. One
     * of the properties is a literal, date, while the other references the creator. The creator
     * type has its own properties. Some of the properties have ontological annotations to clarify
     * their meanings.
     * Takes one optional command line argument for an output path, if not provided, it will write to
     * ./out
     *
     * @param args
     * @throws JsonProcessingException
     */
    public static void main(String[] args) throws JsonProcessingException
    {

        ISchemaFacade schemaFacade =
                new SchemaFacade("name", "description", "2024-12-04T07:53:11Z", "licenceIdentifier",
                        Map.of());

        {
            Type type = new Type();
            type.setId("TextResource");
            type.setSubClassOf(List.of("https://schema.org/Thing"));
            type.setOntologicalAnnotations(
                    List.of("https://www.dublincore.org/specifications/dublin-core/dcmi-terms/dcmitype/Text/"));
            schemaFacade.addType(type);

            Type creatorType = new Type();
            creatorType.setId("Creator");
            creatorType.setSubClassOf(List.of("https://schema.org/Thing"));
            creatorType.setOntologicalAnnotations(
                    List.of("https://www.dublincore.org/specifications/dublin-core/dcmi-terms/terms/creator//"));
            schemaFacade.addType(type);
            schemaFacade.addType(creatorType);
            {

            }



            PropertyType property = new PropertyType();
            property.setId("hasDateSubmitted");
            property.addDataType(LiteralType.DATETIME);
            property.setOntologicalAnnotations(
                    List.of("https://www.dublincore.org/specifications/dublin-core/dcmi-terms/terms/dateSubmitted/"));
            type.addProperty(property);

            PropertyType propertyCreator = new PropertyType();
            propertyCreator.setId("hasCreator");
            propertyCreator.addType(creatorType);
            type.addProperty(propertyCreator);

            PropertyType name = new PropertyType();
            name.setId("hasName");
            name.addDataType(LiteralType.STRING);
            creatorType.addProperty(name);

            PropertyType identifier = new PropertyType();
            identifier.setId("hasIdentifier");
            identifier.addDataType(LiteralType.STRING);
            identifier.setOntologicalAnnotations(
                    List.of("https://www.dublincore.org/specifications/dublin-core/dcmi-terms/elements11/identifier/"));
            creatorType.addProperty(identifier);


            schemaFacade.addPropertyType(property);
            schemaFacade.addPropertyType(propertyCreator);
            schemaFacade.addPropertyType(identifier);
            schemaFacade.addPropertyType(name);

            String creatorId = "creator1";
            IMetadataEntry creatorEntry = new MetadataEntry(creatorId, Set.of(creatorType.getId()),
                    Map.of("hasName", "John Author", "hasIdentifier",
                            "https://orcid.org/0000-0000-0000-0000"), Map.of());

            IMetadataEntry metadataEntry =
                    new MetadataEntry("TextResource1", Set.of("TextResource"),
                            Map.of("hasDate", "2025-01-21T07:12:20Z"),
                            Map.of("hasCreator", List.of(creatorId)));
            schemaFacade.addEntry(metadataEntry);
            schemaFacade.addEntry(creatorEntry);

        }

        String path = args.length >= 1 ? args[0] : "out";




        FolderWriter folderWriter = new FolderWriter();
        folderWriter.save(schemaFacade.getCrate(), path);

    }

}
