package cz.cuni.mff.xrg.odalic.api.rest.resources;

import org.apache.http.HttpHeaders;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

/**
 * @author Tomas Knap
 */
@Ignore
public class AdequateResourceTest {

    @Test
    public void saveToAddequate() {

        AdequateResource ar = new AdequateResource();
        Response out = ar.saveToAddequate(null, "user", "autoimport-KTRPTvAXiXM-MFU");
        assertNotNull(out);

    }

    @Test
    public void parseExecId() {

        String jsonResp = "{\"id\":806,\"status\":\"QUEUED\",\"orderNumber\":1,\"start\":null,\"end\":null,\"schedule\":null,\"stop\":false,\"lastChange\":\"2018-06-29T12:09:38.900+0200\",\"userExternalId\":\"admin\",\"userActorExternalId\":null,\"debugging\":false}";
        AdequateResource ar = new AdequateResource();
        int execId = ar.getExecutionId(jsonResp);
        assertNotNull(execId);
        assertEquals(execId,806);
    }

}
