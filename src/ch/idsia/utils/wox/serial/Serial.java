package ch.idsia.utils.wox.serial;

/**
 * Created by IntelliJ IDEA.
 * User: sml
 * Date: 05-Aug-2004
 * Time: 14:48:57
 * To change this template use Options | File Templates.
 */
public interface Serial
{
    // use string constants to enforce consistency
    // between readers and writers
    String OBJECT = "object";
    String FIELD = "field";
    String NAME = "name";
    String TYPE = "type";
    String VALUE = "value";
    String ARRAY = "array";
    String LENGTH = "length";
    String ID = "id";
    String IDREF = "idref";

    // next is used to disambiguate shadowed fields
    String DECLARED = "declaredClass";


    Class[] primitiveArrays =
            new Class[]{
                    int[].class,
                    boolean[].class,
                    byte[].class,
                    short[].class,
                    long[].class,
                    char[].class,
                    float[].class,
                    double[].class
            };

    // now declare the wrapper classes for each primitive object type
    // note that this order must correspond to the order in primitiveArrays

    // there may be a better way of doing this that does not involve
    // wrapper objects (e.g. Integer is the wrapper of int), but I've
    // yet to find it
    // note that the creation of wrapper objects is a significant
    // overhead
    // example: reading an array of 1 million int (all zeros) takes
    // about 900ms using reflection, versus 350ms hard-coded
    Class[] primitiveWrappers =
            new Class[]{
                    Integer.class,
                    Boolean.class,
                    Byte.class,
                    Short.class,
                    Long.class,
                    Character.class,
                    Float.class,
                    Double.class
            };

    Class[] primitives =
            new Class[]{
                    int.class,
                    boolean.class,
                    byte.class,
                    short.class,
                    long.class,
                    char.class,
                    float.class,
                    double.class
            };
}
