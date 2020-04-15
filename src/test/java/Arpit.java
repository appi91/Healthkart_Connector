import ch.qos.logback.core.net.*;
import com.google.common.base.*;

import java.util.*;

public class Arpit {

    public static void main(String args[]) throws ClassNotFoundException {

       // Date d = Date.parse("2020-01-11T19:59:37");
        System.out.println(int.class);
        Class c = int.class;
        if(c.isPrimitive()) {
            System.out.println("true");
            System.out.println(Boolean.valueOf("as"));
        }
        /*Class c = Class.forName("java.lang.Integer");
        System.out.println(c.cast(1));
        System.out.println();

        System.out.println(Defaults.defaultValue(Integer.TYPE));*/
      /*  List<String> params = new LinkedList<>();
        params.add("A");
        params.add("B.b");
        params.add("C.c.cc");

        int level = 1;
        Map<String,Object> requestStructureRefMap = new HashMap<>();
        Map<String,Object> refMap;
        for(String param : params) {
            String[] depthObj = param.split("\\.");
            System.out.println(param + " splited " + depthObj.length);
            level = 1;
            refMap = requestStructureRefMap;
            //TODO : logic for nth level mapping via reference map
            for (String jsonKey : depthObj) {
                System.out.println(jsonKey);
                if (depthObj.length == level) {
                    refMap.put(jsonKey, null);
                } else {
                    if(refMap.containsKey(jsonKey)) {
                        refMap = (Map<String, Object>) refMap.get(jsonKey);
                    } else {
                        refMap.put(jsonKey, new HashMap<String, Object>());
                        refMap = (Map<String, Object>) refMap.get(jsonKey);
                    }

                }
                level++;
            }


        }

        System.out.println(requestStructureRefMap.toString()); */
    }
}
