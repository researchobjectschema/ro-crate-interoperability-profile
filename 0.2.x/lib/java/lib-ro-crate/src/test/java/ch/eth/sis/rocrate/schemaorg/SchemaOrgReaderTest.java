package ch.eth.sis.rocrate.schemaorg;

import ch.eth.sis.rocrate.facade.IPropertyType;
import ch.eth.sis.rocrate.facade.IType;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;

public class SchemaOrgReaderTest extends TestCase
{

    @Test
    public void testReading() throws FileNotFoundException
    {
        ClassLoader classLoader = getClass().getClassLoader();
        classLoader.getName();
        File file = new File(classLoader.getResource(
                "ch/eth/sis/rocrate/schemaorg/schemaorg-all-https-v29.0.ttl").getFile());

        SchemaOrgInformation information = SchemaOrgReader.read(new FileInputStream(file));

        IType person = information.getIdentifiersToDataTypes().get("schema:Person");
        assertEquals(1, person.getOntologicalAnnotations().size());
        assertTrue(person.getSubClassOf().stream().anyMatch(x -> x.equals("schema:Thing")));
        assertEquals(1, person.getSubClassOf().size());

        IPropertyType firstNameType =
                information.getIdentifiersToPropertyTypes().get("schema:givenName");
        assertTrue(firstNameType.getRange().contains("xsd:string"));
        assertEquals(1, firstNameType.getRange().size());

        {
            IPropertyType nameType =
                    information.getIdentifiersToPropertyTypes().get("schema:name");
            assertTrue(nameType.getRange().contains("xsd:string"));
            assertTrue(nameType.getDomain().contains(person));
            assertEquals(1, nameType.getRange().size());
        }

        assertTrue(firstNameType.getDomain().contains(person));
        assertEquals(2, firstNameType.getDomain().size()); //Person and Patient

        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:isicV4");
            assertTrue(propertyType.getRange().contains("xsd:string"));
            assertEquals(167, propertyType.getDomain().size());

        }

        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:makesOffer");
            assertTrue(propertyType.getDomain().contains(person));
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:Organization")));

            assertEquals(1, propertyType.getRange().size());
            assertTrue(propertyType.getRange().stream().anyMatch(x -> x.equals("schema:Offer")));

        }

        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:abstract");
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:CreativeWork")));

            assertEquals(1, propertyType.getRange().size());
            assertTrue(propertyType.getRange().stream().anyMatch(x -> x.equals("xsd:string")));

        }

        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:comment");
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:CreativeWork")));

            assertEquals(1, propertyType.getRange().size());
            assertTrue(propertyType.getRange().stream().anyMatch(x -> x.equals("schema:Comment")));

        }
        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:isAccessibleForFree");
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:CreativeWork")));
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:Event")));
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:Place")));


            assertEquals(1, propertyType.getRange().size());
            assertTrue(propertyType.getRange().stream().anyMatch(x -> x.equals("xsd:boolean")));

        }

        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:commentCount");
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:CreativeWork")));

            assertEquals(1, propertyType.getRange().size());
            assertTrue(propertyType.getRange().stream().anyMatch(x -> x.equals("xsd:integer")));

        }
        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:url");
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().equals("schema:Thing")));

            assertEquals(1, propertyType.getRange().size());
            assertTrue(propertyType.getRange().stream().anyMatch(x -> x.equals("xsd:anyURI")));

        }

        {
            IType type = information.getIdentifiersToDataTypes().get("schema:MedicalProcedure");
            assertEquals(1, type.getOntologicalAnnotations().size());
            List<IType> superTypes = information.getSuperTypes().get(type);

            assertTrue(
                    type.getSubClassOf().stream().anyMatch(x -> x.equals("schema:MedicalEntity")));

            assertTrue(superTypes.stream().anyMatch(x -> x.getId().equals("schema:Thing")));
            assertTrue(superTypes.stream().anyMatch(x -> x.getId().equals("schema:MedicalEntity")));
            assertEquals(1, type.getSubClassOf().size());

        }
        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:vatID");
            assertTrue(propertyType.getDomain().stream().noneMatch(Objects::isNull));

        }

        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:abstract");
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().contains("CreativeWork")));
        }

        {
            IPropertyType propertyType =
                    information.getIdentifiersToPropertyTypes().get("schema:name");
            assertTrue(propertyType.getDomain().stream()
                    .anyMatch(x -> x.getId().contains("CreativeWork")));
        }



    }

}