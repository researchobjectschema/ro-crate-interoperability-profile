package ch.eth.sis.rocrate.example.doc;

import ch.eth.sis.rocrate.SchemaFacade;
import ch.eth.sis.rocrate.facade.*;
import edu.kit.datamanager.ro_crate.writer.FolderWriter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuickStartWrite
{

    private static final String PREFIX = "Example";

    private static final String SEPARATOR = ":";

    public static final String TMP_EXAMPLE_CRATE = "/tmp/example-crate";

    public static void main(String[] args)
    {
        /* Setting up an RO-Crate with the schema facade */
        ISchemaFacade schemaFacade =
                new SchemaFacade("name", "description", "2024-12-04T07:53:11Z", "licenceIdentifier",
                        Map.of());

        Type personType = new Type();

        {
            personType.setId(PREFIX + SEPARATOR + "Person");
            personType.setOntologicalAnnotations(List.of("https://schema.org/Person"));

            {
                PropertyType personId = new PropertyType();
                personId.setId(PREFIX + SEPARATOR + "personid");
                personId.setTypes(List.of(LiteralType.STRING));
                personType.addProperty(personId);
            }
            {
                PropertyType givenName = new PropertyType();
                givenName.setId(PREFIX + SEPARATOR + "givenName");
                givenName.setOntologicalAnnotations(List.of("https://schema.org/givenName"));
                givenName.setTypes(List.of(LiteralType.STRING));
                personType.addProperty(givenName);
            }
            {
                PropertyType givenName = new PropertyType();
                givenName.setId(PREFIX + SEPARATOR + "familyName");
                givenName.setOntologicalAnnotations(List.of("https://schema.org/familyName"));
                givenName.setTypes(List.of(LiteralType.STRING));
                personType.addProperty(givenName);
            }
            {
                PropertyType identifier = new PropertyType();
                identifier.setId(PREFIX + SEPARATOR + "identifier");
                identifier.setOntologicalAnnotations(List.of("https://schema.org/identifier"));
                identifier.setTypes(List.of(LiteralType.STRING));
                personType.addProperty(identifier);
            }
            schemaFacade.addType(personType);

        }

        Type experimentType = new Type();

        /* Building our Experiment type */
        {
            experimentType.setId(PREFIX + SEPARATOR + "Experiment");

            {
                PropertyType experimentId = new PropertyType();
                experimentId.setId(PREFIX + SEPARATOR + "experimentid");
                experimentId.setTypes(List.of(LiteralType.STRING));
                experimentType.addProperty(experimentId);
            }
            {
                PropertyType creator = new PropertyType();
                creator.setId(PREFIX + SEPARATOR + "creator");
                creator.setOntologicalAnnotations(List.of("https://schema.org/creator"));
                creator.addType(personType);
                experimentType.addProperty(creator);
            }
            {
                PropertyType name = new PropertyType();
                name.setId(PREFIX + SEPARATOR + "name");
                name.setTypes(List.of(LiteralType.STRING));
                experimentType.addProperty(name);
            }
            {
                PropertyType date = new PropertyType();
                date.setId(PREFIX + SEPARATOR + "date");
                date.setTypes(List.of(LiteralType.DATETIME));
                experimentType.addProperty(date);
            }
            schemaFacade.addType(experimentType);

        }

        {
            MetadataEntry personAndreas = new MetadataEntry();
            personAndreas.setId("PERSON1");
            Map<String, Serializable> properties = new LinkedHashMap<>();
            personAndreas.setTypes(Set.of(personType.getId()));
            properties.put("givenname", "Andreas");
            properties.put("lastname", "Meier");
            properties.put("identifier", "https://orcid.org/0009-0002-6541-4637");
            personAndreas.setProps(properties);
            personAndreas.setReferences(new LinkedHashMap<>());
            schemaFacade.addEntry(personAndreas);

            MetadataEntry personJuan = new MetadataEntry();
            personJuan.setId("PERSON2");
            personJuan.setTypes(Set.of(personType.getId()));
            Map<String, Serializable> properties2 = new LinkedHashMap<>();
            properties2.put("givenname", "Andreas");
            properties2.put("lastname", "Meier");
            properties2.put("identifier", "https://orcid.org/0009-0002-6541-4637");
            personJuan.setProps(properties2);
            personJuan.setReferences(new LinkedHashMap<>());

            schemaFacade.addEntry(personJuan);

            MetadataEntry experiment1 = new MetadataEntry();
            experiment1.setId("EXPERIMENT1");
            experiment1.setReferences(Map.of("creator", List.of(personAndreas.getId())));
            experiment1.setTypes(Set.of(experimentType.getId()));
            Map<String, Serializable> propertiesExperiment = new LinkedHashMap<>();
            propertiesExperiment.put("name", "Example Experiment");
            propertiesExperiment.put("date", "2025-09-08 08:41:50.000");
            experiment1.setProps(propertiesExperiment);
            schemaFacade.addEntry(experiment1);

        }

        FolderWriter folderWriter = new FolderWriter();
        folderWriter.save(schemaFacade.getCrate(), TMP_EXAMPLE_CRATE);

    }

}
