package app;

/* Reads the responses of the csv file. Must be downloaded from Google From dashboard */

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CPTFileReader {

    public static HashMap<String, String[]> read(File file) {
        if (!file.exists()) {
            System.out.println("File not exists.");
            throw new RuntimeException("File Not Exists");
        }

//        System.out.println(file.getAbsolutePath());

        /* Key is user name. For now, it is the timestamp.The value is the row of answers.*/
        HashMap<String, String[]> map = new HashMap<>();

        try (Scanner sc = new Scanner(file)) {
            // skip header row
            if (sc.hasNextLine()) {
                sc.nextLine();
            }

            int id = 0;

            while (sc.hasNextLine()) {

                // TODO: Change this to 1 as we will access the name of the person not the timestamp.
                String[] row = sc.nextLine().split(",");
//                map.put(row[0], row);
                map.put(String.valueOf(id), row);
                id++;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return map;
    }

    public static List<Axis> score(HashMap<String, String[]> map, String id, CPTPlotter cptPlotter) {
        if (map == null || !map.containsKey(id)) {
            return null;
        }

        String[] responses = map.get(id);

        // TODO: We can change the start index to 1 or 2 depending on which column the first question starts...

        HashMap<String, Integer> points = new HashMap<>();

        points.put("Never", 1);
        points.put("Rarely", 2);
        points.put("Sometimes", 3);
        points.put("Often", 4);
        points.put("Always", 5);

        Axis[] axis = CPT.createAxis();

        int numAxis = axis.length;

        for (int i = 1; i < responses.length; i++) {
            Axis ax = axis[i % numAxis];

            String response = responses[i];
//            System.out.println("Question " + i + ": " + response);

            int pt = points.getOrDefault(response, 0);
            ax.update(pt);
        }

        /* Only for logging purpose */
        for (Axis ax : axis) {
//            System.out.println(ax.toString());
            cptPlotter.updateInfoMessage(ax.toString());
        }

        return Arrays.asList(axis);
    }

}
