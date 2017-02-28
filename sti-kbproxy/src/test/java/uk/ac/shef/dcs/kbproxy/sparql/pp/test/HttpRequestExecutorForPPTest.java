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

//    private static HttpRequestExecutor mockHttpExecutorWithErrorResponses() throws Exception {
//        HttpRequestExecutor executor = Mockito.mock(HttpRequestExecutor.class);
//        Mockito.when(executor.sendGetRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));
//        Mockito.when(executor.sendMultipartPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));
//        Mockito.when(executor.sendRawDataPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));
//        Mockito.when(executor.sendFilePostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(File.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));
//
//        return executor;
//    }
//
//    private HttpRequestExecutor mockHttpExecutorWithResponses(HttpRequestConfig_V1 config) throws Exception {
//        HttpRequestExecutor executor = Mockito.mock(HttpRequestExecutor.class);
//
//        StatusLine statusLine = Mockito.mock(StatusLine.class);
//        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
//        Mockito.when(statusLine.toString()).thenReturn("200 OK");
//
//        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
//        HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
//        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
//
//        URI responseResource = null;
//        if (config.getRequestType() == RequestType.GET) {
//            responseResource = this.getClass().getClassLoader().getResource(GET_RESPONSE_FILE).toURI();
//        } else {
//            if (config.getPostRequestDataType() == DataType.FORM_DATA) {
//                responseResource = this.getClass().getClassLoader().getResource(POST_MULTIPART_RESPONSE_FILE).toURI();
//            } else if (config.getPostRequestDataType() == DataType.RAW_DATA) {
//                responseResource = this.getClass().getClassLoader().getResource(POST_RAW_RESPONSE_FILE).toURI();
//            }
//        }
//        File responseFile = new File(responseResource);
//        Mockito.when(httpEntity.getContent()).thenReturn(FileUtils.openInputStream(responseFile));
//        Mockito.when(response.getEntity()).thenReturn(httpEntity);
//
//        Mockito.when(executor.sendGetRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
//        Mockito.when(executor.sendMultipartPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
//        Mockito.when(executor.sendRawDataPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
//        Mockito.when(executor.sendFilePostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(File.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
//
//        return executor;
//    }

//    @Before
//    public void before() throws Exception {
//        this.env = new TestEnvironment();
//        this.output = this.env.createFilesOutput("requestOutput");
//
//        this.dpu = new HttpRequest();
//        this.dpu.client = HttpClients.createDefault();
//    }
//
//    @After
//    public void after() throws Exception {
//        this.env.release();
//    }

    //create class
    //@Test
    public void POSTCreateClass() throws Exception {

        HttpRequestExecutorForPP executor = new HttpRequestExecutorForPP();
        ResourceDesc ed = new ResourceDesc("http://example.org/class/test03", "test03"); //the url is the class URL
        //String url = executor.createConceptRequest(ed);
        //ed.setConceptUrl(url);
        executor.createClassRequest(ed);
    }


    //create relation
    @Test
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
        ResourceDesc ed = new ResourceDesc("test01");
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


//    @Test(expected = DPUException.class)
//    public void rawPOSTRequestResponseErrorCodeTest() throws Exception {
//        HttpRequestConfig_V1 config = createPostRawRequestConfig();
//        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
//        HttpRequestExecutor executor = mockHttpExecutorWithErrorResponses();
//        this.dpu.setRequestExecutor(executor);
//        this.env.run(this.dpu);
//    }
//
//    @Test(expected = DPUException.class)
//    public void multipartPOSTResponseErrorCodeTest() throws Exception {
//        HttpRequestConfig_V1 config = createPostMultipartRequestConfig();
//        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
//        HttpRequestExecutor executor = mockHttpExecutorWithErrorResponses();
//        this.dpu.setRequestExecutor(executor);
//        this.env.run(this.dpu);
//    }
//
//    @Test
//    public void GETRequestTest() throws Exception {
//        HttpRequestConfig_V1 config = createGetRequestConfig();
//
//        URI sentResponse = this.getClass().getClassLoader().getResource(GET_RESPONSE_FILE).toURI();
//        String sentResponseContent = readFile(new File(sentResponse));
//
//        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
//        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
//        this.dpu.setRequestExecutor(executor);
//        this.env.run(this.dpu);
//
//        FilesDataUnit.Entry entry = this.output.getIteration().next();
//        File receivedResponse = new File(URI.create(entry.getFileURIString()));
//        String receivedResponseContent = readFile(receivedResponse);
//
//        assertEquals(sentResponseContent, receivedResponseContent);
//    }
//
//    @Test
//    public void rawPOSTRequestTest() throws Exception {
//        HttpRequestConfig_V1 config = createPostRawRequestConfig();
//
//        URI sentResponse = this.getClass().getClassLoader().getResource(POST_RAW_RESPONSE_FILE).toURI();
//        String sentResponseContent = readFile(new File(sentResponse));
//
//        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
//        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
//        this.dpu.setRequestExecutor(executor);
//        this.env.run(this.dpu);
//
//        FilesDataUnit.Entry entry = this.output.getIteration().next();
//        File receivedResponse = new File(URI.create(entry.getFileURIString()));
//        String receivedResponseContent = readFile(receivedResponse);
//
//        assertEquals(sentResponseContent, receivedResponseContent);
//    }
//
//    @Test
//    public void multipartPOSTRequestTest() throws Exception {
//        HttpRequestConfig_V1 config = createPostMultipartRequestConfig();
//
//        URI sentResponse = this.getClass().getClassLoader().getResource(POST_MULTIPART_RESPONSE_FILE).toURI();
//        String sentResponseContent = readFile(new File(sentResponse));
//
//        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
//        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
//        this.dpu.setRequestExecutor(executor);
//        this.env.run(this.dpu);
//
//        FilesDataUnit.Entry entry = this.output.getIteration().next();
//        File receivedResponse = new File(URI.create(entry.getFileURIString()));
//        String receivedResponseContent = readFile(receivedResponse);
//
//        assertEquals(sentResponseContent, receivedResponseContent);
//    }
//
//    private static HttpRequestConfig_V1 createGetRequestConfig() {
//        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
//        config.setRequestType(RequestType.GET);
//        config.setRequestURL("http://echo.jsontest.com/key/value/one/two");
//        config.setFileName("get_response.json");
//
//        return config;
//    }
//
//    private static HttpRequestConfig_V1 createPostRawRequestConfig() {
//        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
//        config.setRequestType(RequestType.POST);
//        config.setRequestURL("http://www.webservicex.net/globalweather.asmx");
//        config.setContentType(RequestContentType.SOAP);
//        config.setPostRequestDataType(DataType.RAW_DATA);
//        config.setRawRequestBody("SOAP ENVELOPE TEXT");
//        config.setFileName("raw_response.xml");
//
//        return config;
//    }
//
//    private static HttpRequestConfig_V1 createPostMultipartRequestConfig() {
//        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
//        config.setRequestType(RequestType.POST);
//        config.setRequestURL("https://data.gov.sk/api/action/internal_api");
//        config.setPostRequestDataType(DataType.FORM_DATA);
//        config.setFileName("multipart_response.xml");
//
//        Map<String, String> formData = new HashMap<String, String>();
//        formData.put("action", "resource_show");
//        formData.put("token", "ee^sd5&78");
//        formData.put("user_id", "8EEBA23B-6BFE-4F76-A3B5-D9B627823137");
//        formData.put("data", "{\"id\":\"0bb71eaf-775f-4dac-bcf6-cfa457f816fa\"}");
//
//        config.setFormDataRequestBody(formData);
//
//        return config;
//    }
//
//    private String readFile(File input) {
//        try (FileInputStream inputStream = new FileInputStream(input)) {
//            return IOUtils.toString(inputStream);
//        } catch (IOException ex) {
//            LOG.error("", ex);
//        }
//        return null;
//    }

}
