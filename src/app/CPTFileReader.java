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

        /* Key is user name. For now, it is the timestamp.The value is the row of answers.*/
        HashMap<String, String[]> map = new HashMap<>();

        try (Scanner sc = new Scanner(file)) {
            // skip header row
            if (sc.hasNextLine()) {
                sc.nextLine();
            }

            while (sc.hasNextLine()) {
                String[] row = sc.nextLine().split(",");
                map.put(row[1], row);
                System.out.println("Inserting Entry with Key: " + row[1]);
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

        HashMap<String, Integer> points = new HashMap<>();

        points.put("Never", 1);
        points.put("Rarely", 2);
        points.put("Sometimes", 3);
        points.put("Often", 4);
        points.put("Always", 5);

        Axis[] axis = CPT.createAxis();

        int numAxis = axis.length;

        for (int i = 2; i < responses.length; i++) {
            Axis ax = axis[i % numAxis];

            String response = responses[i];

            int pt = points.getOrDefault(response, 0);
            ax.update(pt);
        }

        for (Axis ax : axis) {
            cptPlotter.updateInfoMessage(ax.toString());
        }

        return Arrays.asList(axis);
    }

}
