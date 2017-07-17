package uk.ac.shef.dcs.kbproxy.sparql.pp.test;

import org.junit.Test;

import uk.ac.shef.dcs.kbproxy.sparql.pp.HttpRequestExecutorForPP;
import uk.ac.shef.dcs.kbproxy.sparql.pp.RelationDesc;
import uk.ac.shef.dcs.kbproxy.sparql.pp.ResourceDesc;

public class HttpRequestExecutorForPPTest {

    //private static final Logger LOG = LoggerFactory.getLogger(HttpRequestExecutorForPPTest.class);

    private static final String GET_RESPONSE_FILE = "get_response.json";

    private static final String POST_RAW_RESPONSE_FILE = "post_raw_response.xml";

    private static final String POST_MULTIPART_RESPONSE_FILE = "post_multipart_response.json";


    //create class
    @Test
    public void POSTCreateClass() throws Exception {

        HttpRequestExecutorForPP executor = new HttpRequestExecutorForPP();
        ResourceDesc ed = new ResourceDesc("http://example.org/object/xx-test/", "xx-test"); //the url is the class URL
        //ResourceDesc ed = new ResourceDesc("http://example.org/subjecthttp://example.org/class/test03", "test03"); //the url is the class URL
        //String url = executor.createConceptRequest(ed);
        //ed.setConceptUrl(url);
        executor.createClassRequest(ed);
    }


    //create relation
    //@Test
    public void POSTCreateRelation() throws Exception {

        HttpRequestExecutorForPP executor = new HttpRequestExecutorForPP();
        RelationDesc ed = new RelationDesc("http://example.org/class/relation02", "relation", "http://example.org/class/test03","http://example.org/class/test04"); //the url is the class URL
        //String url = executor.createConceptRequest(ed);
        //ed.setConceptUrl(url);
        executor.createRelationRequest(ed);
    }


    //create concept
    //@Test
    public void POSTCreateConcept() throws Exception {

        HttpRequestExecutorForPP executor = new HttpRequestExecutorForPP();
        ResourceDesc ed = new ResourceDesc("test0XX");
        executor.createConceptRequest(ed);

    }

    //create concept & add alternative labels
    //@Test
    public void POSTCreateConceptAddAltLabels() throws Exception {

        HttpRequestExecutorForPP executor = new HttpRequestExecutorForPP();
        ResourceDesc ed = new ResourceDesc("test02");
        String url = executor.createConceptRequest(ed);
        ed.setConceptUrl(url);
        executor.addLiteralRequest(ed,"test02a");
        executor.addLiteralRequest(ed,"test02b");
    }

    //@Before
    public void createClass() {
        HttpRequestExecutorForPP executor = new HttpRequestExecutorForPP();
        ResourceDesc ed = new ResourceDesc("http://example.org/class/test04", "test04"); //the url is the class URL
        executor.createClassRequest(ed);
    }

    //create concept and apply to it class http://example.org/class/test03
    //@Test
    public void POSTCreateConceptApplyClassToThat() throws Exception {

        HttpRequestExecutorForPP executor = new HttpRequestExecutorForPP();
        ResourceDesc ed = new ResourceDesc("test04");
        String url = executor.createConceptRequest(ed);
        ed.setConceptUrl(url);
        executor.applyTypeRequest(ed,"http://example.org/class/test04");
    }

}
