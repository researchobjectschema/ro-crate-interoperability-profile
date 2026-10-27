package ch.eth.sis.rocrate.example;

import ch.eth.sis.rocrate.SchemaFacade;
import ch.eth.sis.rocrate.read.RoCrateLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.datamanager.ro_crate.RoCrate;

public class ReadExample
{
    /**
     * This reads an RO-Crate with schema information and prints out
     * If you need a compatible RO-Crate, run WriteExample.java first.
     * Takes one optional command line argument for an output path, if not provided, it will read from
     * ./out
     * @param args
     * @throws JsonProcessingException
     */
    public static void main(String[] args) throws JsonProcessingException
    {
        String path = args.length >= 1 ? args[0] : "out";
        RoCrate crate = RoCrateLoader.read(path);
        SchemaFacade schemaFacade = SchemaFacade.of(crate);
        schemaFacade.getTypes().forEach(
                x -> System.out.println("RDFS Class " + x.getId())
        );
        schemaFacade.getPropertyTypes().forEach(
                x -> System.out.println("RDFS Property " + x.getId())
        );
        schemaFacade.getEntries(schemaFacade.getTypes().get(0).getId()).forEach(
                x -> System.out.println("Metadata entry " + x.getId())
        );

    }


}
