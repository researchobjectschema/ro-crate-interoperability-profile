package ch.eth.sis.rocrate.backwardcompatibility;

import ch.eth.sis.rocrate.SchemaFacade;
import ch.eth.sis.rocrate.facade.IPropertyType;
import ch.eth.sis.rocrate.read.RoCrateLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.datamanager.ro_crate.RoCrate;
import junit.framework.TestCase;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RdfPropertyBackwardCompatibilityTest extends TestCase
{

    public static final String INPUT_NEW =
            "src/test/resources/ch/eth/sis/rocrate/property/backwardcompatibility/rdfProperty";

    public static final String INPUT_OLD =
            "src/test/resources/ch/eth/sis/rocrate/property/backwardcompatibility/rdfsProperty";

    public void testBackwardCompatibility() throws JsonProcessingException
    {
        RoCrate crateNew = RoCrateLoader.read(INPUT_NEW);
        RoCrate crateOld = RoCrateLoader.read(INPUT_OLD);
        SchemaFacade schemaFacadeOld = SchemaFacade.of(crateOld);
        SchemaFacade schemaFacadeNew = SchemaFacade.of(crateNew);

        Map<String, IPropertyType> oldIdTopropertyType = schemaFacadeOld.getPropertyTypes().stream()
                .collect(Collectors.toMap(x -> x.getId(), Function.identity()));

        Map<String, IPropertyType> newIdToPropertyType = schemaFacadeNew.getPropertyTypes().stream()
                .collect(Collectors.toMap(x -> x.getId(), Function.identity()));


        for (IPropertyType propertyTypeNew : schemaFacadeNew.getPropertyTypes())
        {
            assertTrue(oldIdTopropertyType.containsKey(propertyTypeNew.getId()));

        }

        for (IPropertyType propertyTypeNew : schemaFacadeOld.getPropertyTypes())
        {
            assertTrue(newIdToPropertyType.containsKey(propertyTypeNew.getId()));

        }


    }

}
