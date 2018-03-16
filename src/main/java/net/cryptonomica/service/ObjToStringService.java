package net.cryptonomica.service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ObjToStringService {

    /* see:
    * http://stackoverflow.com/questions/17095628/loop-over-all-fields-in-a-java-class
    * */
    public String ObjToMap (Object obj) {

        Class<?> c = obj.getClass();

        Field[] fields = c.getDeclaredFields();

        Map<String, Object> temp = new HashMap<String, Object>();

        for( Field field : fields ){
            try {

                temp.put(field.getName(), field.get(obj));

            } catch (IllegalArgumentException e) {
                e.printStackTrace();

            } catch (IllegalAccessException e2) {
                e2.printStackTrace();

            }
        }

        return null;
    }
}
