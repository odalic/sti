package uk.ac.shef.dcs.sti.todo;

import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.freebase.FreebaseSearch;
import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 21/04/2015.
 */
public class FreebaseTypeGranularityPopulator {
    protected static final String PROPERTY_HOME = "sti.home";
    protected static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

    public static void main(String[] args) throws IOException, ClassNotFoundException, KBProxyException {
        //fetch freebase pages, parse them and get granularity scores
        List<String> all_types = new ArrayList<>(new HashSet<>(FileUtils.readList(args[1] + "/types_merge_all.txt", false)));
        Collections.sort(all_types);
        
        Properties properties = new Properties();
        properties.load(new FileReader(new File(args[2])));
        FreebaseSearch kbSeacher =
                new FreebaseSearch(properties, true, getAbsolutePath(properties, PROPERTY_CACHE_FOLDER), null);
        kbSeacher.initializeCaches();

        //kbSeacher.find_triplesForProperty("/award/award_category/nomination_announcement");

        System.out.println("total = " + all_types.size());
        int count = 0;
        for (String t : all_types) {
            System.out.println(count + "_" + t);
            try {
                kbSeacher.findGranularityOfClazz(t);
                kbSeacher.findAttributesOfClazz(t);
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
        kbSeacher.closeConnection();
        System.exit(0);
    }

    static String getAbsolutePath(Properties properties, String propertyName) {
        return properties.getProperty(PROPERTY_HOME)
                + File.separator + properties.getProperty(propertyName);
    }
}
