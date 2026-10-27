package ch.eth.sis.rocrate.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.entities.AbstractEntity;

import java.util.ArrayList;
import java.util.List;

public class RoCrateValueUtil
{

    public static List<String> parseMultiValued(AbstractEntity dataEntity, String key)
    {
        JsonNode node = dataEntity.getProperty(key);
        return parseMultiValued(node);

    }

    public static List<String> parseMultiValued(JsonNode jsonNode)
    {

        if (jsonNode == null)
        {
            return List.of();
        }

        if (jsonNode.isTextual())
        {
            return List.of(jsonNode.asText());
        }

        if (jsonNode instanceof ObjectNode)
        {
            return List.of(jsonNode.get("@id").textValue());
        }
        if (jsonNode instanceof ArrayNode arrayNode)
        {
            List<String> accumulator = new ArrayList<>();
            arrayNode.elements().forEachRemaining(
                    x -> {
                        if (x.isTextual())
                        {
                            accumulator.add(x.asText());
                        } else
                        {
                            accumulator.add(x.get("@id").textValue());
                        }
                    }
            );
            return accumulator;
        }
        return List.of();

    }

}
