import org.json.JSONObject;

/**
 * Created by warrior on 09.01.15.
 */
public class Card {

    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String RARITY = "rarity";
    private static final String TYPE = "type";
    private static final String COLLECTIBLE = "collectible";
    private static final String COST = "cost";
    private static final String HEALTH = "health";
    private static final String ATTACK = "attack";
    private static final String DURABILITY = "durability";
    private static final String RACE = "race";

    private static final String DELIMITER = ", ";

    private static int globalID = 1;

    private final JSONObject object;
    private final int id;

    public Card(JSONObject object) {
        this.object = object;
        id = globalID++;
    }

    public int getId() {
        return id;
    }

    public JSONObject getObject() {
        return object;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(id).append(DELIMITER);
        builder.append(getValue(object, NAME)).append(DELIMITER);
        builder.append(getValue(object, TEXT)).append(DELIMITER);
        builder.append(getValue(object, RARITY)).append(DELIMITER);
        builder.append(getValue(object, TYPE)).append(DELIMITER);
        builder.append(object.optBoolean(COLLECTIBLE, false)).append(DELIMITER);
        builder.append(getValue(object, COST)).append(DELIMITER);
        builder.append(getValue(object, HEALTH)).append(DELIMITER);
        builder.append(getValue(object, ATTACK)).append(DELIMITER);
        builder.append(getValue(object, DURABILITY)).append(DELIMITER);
        builder.append(getValue(object, RACE));
        builder.append(")");
        return builder.toString();
    }

    private static String wrap(String str) {
        return "'" + str + "'";
    }

    private static String getValue(JSONObject object, String key) {
        if (object.has(key)) {
            Object o = object.get(key);
            if (o instanceof String) {
                String str = (String) o;
                if (str.contains("'")) {
                    return "E" + wrap(str.replaceAll("'", "\\\\'"));
                }
                return wrap(str);
            } else {
                return o.toString();
            }
        } else {
            return "NULL";
        }
    }
}
