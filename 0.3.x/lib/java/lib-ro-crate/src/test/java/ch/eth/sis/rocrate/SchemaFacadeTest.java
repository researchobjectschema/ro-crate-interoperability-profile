package ch.eth.sis.rocrate;

import ch.eth.sis.rocrate.facade.IMetadataEntry;
import ch.eth.sis.rocrate.facade.MetadataEntry;
import ch.eth.sis.rocrate.facade.Type;
import edu.kit.datamanager.ro_crate.RoCrate;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchemaFacadeTest extends TestCase
{

    public static final String BOOL = "Bool";

    public static final String BOOLS = "Bools";

    public static final String STRINGS = "Strings";

    public static final String STRING = "String";

    public static final String DOUBLES = "Doubles";

    public static final String DOUBLE = "Double";

    public static final String INTS = "Ints";

    public static final String INT = "Int";

    public void testWrittenPropertiesAreThere()
    {

        String id = "www.test.org/example";
        String type = "example:thing";
        Set<String> types = Set.of(type);
        Map<String, Serializable> properties = new LinkedHashMap<>();
        properties.put(BOOL, Boolean.TRUE);
        properties.put(BOOLS, new Boolean[] { Boolean.TRUE, Boolean.FALSE });
        properties.put(INT, 1);
        properties.put(INTS, new Integer[] { 2, 3 });
        properties.put(DOUBLE, 0.2);
        properties.put(DOUBLES, new Double[] { 0.0, 0.1 });
        properties.put(STRING, "0.2");
        properties.put(STRINGS, new String[] { "0.0", "0.1" });

        MetadataEntry metadataEntry =
                new MetadataEntry(id, types, properties, new LinkedHashMap<>());

        RoCrate roCrate = new RoCrate.RoCrateBuilder().build();
        SchemaFacade schemaFacade = new SchemaFacade(roCrate);
        Type rdfsClass = new Type();
        rdfsClass.setId(type);
        schemaFacade.addType(rdfsClass);

        schemaFacade.addEntry(metadataEntry);
        List<IMetadataEntry> entries = schemaFacade.getEntries(type);
        assertEquals(1, entries.size());
        IMetadataEntry metadataentry1 = entries.get(0);
        assertTrue(metadataentry1.getValues().get(BOOL) instanceof Boolean);
        assertTrue(metadataentry1.getValues().get(BOOLS) instanceof Boolean[]);
        assertTrue(metadataentry1.getValues().get(INT) instanceof Integer);
        assertTrue(metadataentry1.getValues().get(INTS) instanceof Integer[]);
        assertTrue(metadataentry1.getValues().get(DOUBLE) instanceof Double);
        assertTrue(metadataentry1.getValues().get(DOUBLES) instanceof Double[]);
        assertTrue(metadataentry1.getValues().get(STRING) instanceof String);
        assertTrue(metadataentry1.getValues().get(STRINGS) instanceof String[]);

    }

}