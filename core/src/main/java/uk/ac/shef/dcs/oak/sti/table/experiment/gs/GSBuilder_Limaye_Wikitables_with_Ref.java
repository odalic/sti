package uk.ac.shef.dcs.oak.sti.table.experiment.gs;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.shef.dcs.oak.sti.table.interpreter.maincol.ColumnFeatureGenerator;
import uk.ac.shef.dcs.oak.sti.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.table.rep.*;
import uk.ac.shef.dcs.oak.sti.table.validator.TabValGeneric;
import uk.ac.shef.dcs.oak.sti.table.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.oak.sti.table.xtractor.TableNormalizerFrequentRowLength;
import uk.ac.shef.dcs.oak.sti.table.xtractor.TableObjCreatorWikipediaGS;
import uk.ac.shef.dcs.oak.sti.table.xtractor.TableXtractorWikipedia;
import uk.ac.shef.dcs.oak.sti.test.LimayeDatasetLoader;
import uk.ac.shef.dcs.oak.sti.util.GenericSearchCache_SOLR;
import uk.ac.shef.dcs.oak.triplesearch.freebase.FreebaseQueryHelper;
import uk.ac.shef.dcs.oak.util.FileUtils;
import uk.ac.shef.dcs.oak.websearch.bing.v2.BingWebSearch;
import uk.ac.shef.dcs.oak.websearch.bing.v2.BingWebSearchResultParser;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;

/**
 * for each table in Limaye dataset, try to match columns with corresponding tables in the most up-to-date wikipedia page table;
 * then each cell is searched in the column, if a cell text matches a link text in any cell in the column, it is assigned the link
 */
public class GSBuilder_Limaye_Wikitables_with_Ref extends GSBuilder_Limaye_Wikitables {
    private Levenshtein stringSim = new Levenshtein();

    public GSBuilder_Limaye_Wikitables_with_Ref(FreebaseQueryHelper queryHelper,
                                                GenericSearchCache_SOLR cache_solr,
                                                TableXtractorWikipedia xtractor,
                                                String... bingApiKeys) {
        this.queryHelper = queryHelper;
        this.solrCache = cache_solr;
        this.xtractor = xtractor;
        searcher = new BingWebSearch(bingApiKeys);
        parser = new BingWebSearchResultParser();
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
/*
        find_missed_files_by_folder("E:\\Data\\table annotation\\limaye\\all_tables_freebase_groundtruth",
                "E:\\Data\\table annotation\\limaye\\all_tables_groundtruth_xml_only",
                "E:\\Data\\table annotation\\limaye/gs_limaye_empty.missed");
        System.exit(0);*/

        /* find_missed_files("E:\\Data\\table annotation\\limaye/gs_limaye.e8031313", "E:\\Data\\table annotation\\limaye/gs_limaye.missed");
        System.exit(0);*/


        FreebaseQueryHelper queryHelper = new FreebaseQueryHelper(args[2]);
        String in_original_limaye_folder = args[0];
        String out_gs_folder = args[1];
        String solrCache = args[3];
        int startfrom = new Integer(args[4]);
        Map<String, String> missedFile = new HashMap<String, String>();

        if (args.length == 6) {
            for (String l : FileUtils.readList(args[5], false)) {
                String[] parts = l.split("\t\t\t");
                missedFile.put(parts[0].trim(), parts[1].trim());
            }
        }

        File configFile = new File(solrCache + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(solrCache,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");
        GenericSearchCache_SOLR cache = new GenericSearchCache_SOLR(server);

        TableXtractorWikipedia xtractor = new TableXtractorWikipedia(new TableNormalizerFrequentRowLength(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorWikipediaGS(false),
                new TabValGeneric());

        GSBuilder_Limaye_Wikitables_with_Ref gsBuilder = new GSBuilder_Limaye_Wikitables_with_Ref(queryHelper,
                cache, xtractor,
                "nKegYOqCMXV0rjUHzKADJinbJ9NrkMyBMqm9h3X9vAo");

        int count = 0;
        File[] all = new File(in_original_limaye_folder).listFiles();
        List<File> sorted = new ArrayList<File>(Arrays.asList(all));
        Collections.sort(sorted);
        System.out.println(all.length);
        for (File f : sorted) {
            try {
                count++;
                if (startfrom > count)
                    continue;
                if (f.getName().startsWith("file")) {
                    System.err.println("ERROR:SKIPPED_NON_WIKI:" + f.getName());
                    continue;
                }

                System.out.println(count + "_" + f);
                LTable limaye_table = LimayeDatasetLoader.readTable(f.toString(), null, null);

                String wikiPage = null;
                if (missedFile.size() > 0) {
                    for (String mf : missedFile.keySet()) {
                        if (f.toString().endsWith(mf)) {
                            wikiPage = fetchWikipediaWebpage(missedFile.get(mf));
                            break;
                        }
                    }
                    if (wikiPage == null)
                        continue;                                                      //Ã‰ric Cantona
                } else {                                                               //MUST REGEN GS FOR 112 TABLES!!!
                    //multiword
                    //what is corresponding wikipedia title
                    int dot_html = f.getName().indexOf(".htm");
                    if (dot_html == -1)
                        dot_html = f.getName().length();
                    String wikiTitle = f.getName().substring(0, dot_html);

                    if (wikiTitle.indexOf("~") != -1) {
                        System.err.println("ERROR:~DETECTED:" + f.getName());
                        continue;
                    }

                    try {
                        wikiPage = fetchWikipediaWebpage(wikipediaURL + wikiTitle);
                    } catch (Exception e) {
                        wikiTitle = parseToWikipediaTitle(f.toString());
                        try {
                            wikiPage = fetchWikipediaWebpage(wikipediaURL + wikiTitle);
                        } catch (Exception ee) {
                        }
                    }
                    if (wikiPage == null || wikiPage.length() == 0) {
                        wikiPage = gsBuilder.tryWebSearch(wikiTitle);
                        if (wikiPage == null) {
                            System.err.println("ERROR:NO_WIKIPAGE:" + f.getName());
                            continue;
                        }
                    }
                }
                wikiPage = StringUtils.stripAccents(wikiPage);

                List<Node> candidates = extractWikiTables(wikiPage, f.toURI().toString());
                Node theOne = findMatchingTable(limaye_table, candidates);
                if (theOne == null) {
                    System.err.println("ERROR:NO_TABLE:" + f.getName());
                    continue;
                }
                LTable wikitable = gsBuilder.
                        process_wikitable(theOne, f.toURI().toString(), f.toURI().toString(), limaye_table.getContexts().toArray(new LTableContext[0]));
                if (wikitable == null) {
                    System.err.println("ERROR:IRREGULAR_TABLE:" + f.getName());
                    continue;
                }

                gsBuilder.annotateTable(wikitable, limaye_table,
                        out_gs_folder + "/" + f.getName() + ".cell.keys");
                gsBuilder.saveAsLimaye(wikitable, out_gs_folder + "/" + f.getName());


            } catch (Exception e) {
                System.err.println("ERROR:UNKNOWN:" + f.getName());
                e.printStackTrace();
            }
        }
        server.shutdown();
        System.exit(0);
    }


    private void annotateTable(LTable wikitable, LTable limaye_table,
                               String annotationFile) throws IOException, TransformerException, ParserConfigurationException {
        //saveAsLimaye(table, rawFile);
        StringBuilder annotation = new StringBuilder();
        ColumnFeatureGenerator.feature_columnDataTypes(limaye_table);

        for (int c = 0; c < limaye_table.getNumCols(); c++) {
            DataTypeClassifier.DataType type = limaye_table.getColumnHeader(c).getTypes().get(0).getCandidateType();
            if (type.equals(DataTypeClassifier.DataType.NUMBER) || type.equals(DataTypeClassifier.DataType.DATE) ||
                    type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER) ||
                    type.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                    type.equals(DataTypeClassifier.DataType.LONG_STRING))
                continue;

            //find matching column from wikitable
            int matchedCol = findMatchingColumn(limaye_table, c, wikitable);
            if (matchedCol == -1) {
                System.err.println("\tERROR:no matching column=" + c + "," + limaye_table.getColumnHeader(c).getHeaderText());
                continue;
            }
            //annotate
            for (int r = 0; r < limaye_table.getNumRows(); r++) {
                String limaye_cell_text = limaye_table.getContentCell(r, c).getText();
                limaye_cell_text=StringUtils.stripAccents(limaye_cell_text);

                double bestScore = 0.0;
                CellAnnotation bestAnnotation = null;
                for (int wr = 0; wr < wikitable.getNumRows(); wr++) {
                    CellAnnotation[] annotations = wikitable.getTableAnnotations().getContentCellAnnotations(wr, matchedCol);
                    for (CellAnnotation ca : annotations) {
                        double matched_score = stringSim.getSimilarity(limaye_cell_text,
                                ca.getTerm());
                        if (matched_score > bestScore) {
                            bestScore = matched_score;
                            bestAnnotation = ca;
                        }
                    }
                }

                if (bestScore > 0.9) {
                    if (bestScore != 1.0) {
                        System.out.println("\t\t\tNoPerfectMatch:" + limaye_cell_text + "(limaye)," + bestAnnotation.getTerm() + "(wiki)");
                    }

                    String wikiTitle = bestAnnotation.getAnnotation().getId();
                    if (wikiTitle.startsWith("/wiki/"))
                        wikiTitle = wikiTitle.substring(6).trim();
                    String pageid = queryWikipediaPageid(wikiTitle, solrCache);
                    if (pageid != null) {
                        String fb_id = createCellAnnotation(pageid, solrCache);
                        if (fb_id != null && fb_id.length() > 0) {
                            annotation.append(r + "," + c + "=").append(fb_id).append("\n");
                        }
                    }
                }
            }
        }

        PrintWriter p = new PrintWriter(annotationFile);
        p.println(annotation);
        p.close();
    }

    private int findMatchingColumn(LTable limaye_table, int c, LTable wikitable) {
        double maxScore = 0.0;
        int theOne = -1;
        String column = toString_Column(limaye_table, c);
        column=StringUtils.stripAccents(column);
        for (int wc = 0; wc < wikitable.getNumCols(); wc++) {
            String columnContent = toString_Column_Annotation(wikitable, wc);
            double overlap = computeOverlap(column, columnContent);
            if (overlap > maxScore) {

                maxScore = overlap;
                theOne = wc;
            }
        }

        return theOne;
    }


    private String toString_Column(LTable table, int c) {
        String text = "";

        for (int r = 0; r < table.getNumRows(); r++) {
            LTableContentCell tcc = table.getContentCell(r, c);
            text += tcc.getText() + " ";
        }
        return text;
    }

    private String toString_Column_Annotation(LTable table, int c) {
        String text = "";

        for (int r = 0; r < table.getNumRows(); r++) {
            CellAnnotation[] annotations = table.getTableAnnotations().getContentCellAnnotations(r, c);
            for (CellAnnotation ca : annotations) {
                text += ca.getTerm() + " ";
            }
        }
        return text;
    }

}
