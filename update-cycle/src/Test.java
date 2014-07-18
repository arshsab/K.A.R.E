import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

/**
 * @author arshsab
 * @since 07 2014
 */

public class Test {
    public static void main(String... args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = mapper.readTree("{\n" +
                "  \"message\": \"Repository access blocked\",\n" +
                "  \"block\": {\n" +
                "    \"reason\": \"dmca\",\n" +
                "    \"created_at\": \"2014-07-03T12:27:59-07:00\"\n" +
                "  }\n" +
                "}");

        System.out.println(node.has("block"));
    }
}
