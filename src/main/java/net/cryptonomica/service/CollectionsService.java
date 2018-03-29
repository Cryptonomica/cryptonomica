package net.cryptonomica.service;

import java.util.Map;

public class CollectionsService {

    public static String mapToString(Map<String, Object> map) {

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            sb.append(entry.getKey().toString());
            sb.append(" : ");
            sb.append(entry.getValue().toString());
            sb.append("\n");
        }

        return sb.toString();
    }

}
