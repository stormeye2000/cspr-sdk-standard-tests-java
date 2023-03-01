import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.service.CasperService;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;

public class GetBlockTest {

    protected static CasperService casperServiceNctl;

    @BeforeAll
    static void init() throws MalformedURLException {
        casperServiceNctl = CasperService.usingPeer("127.0.0.1",
                11101);
    }

    @Test
    void getLastBlock(){
        final JsonBlockData blockData = casperServiceNctl.getBlock();
        assertNotNull(blockData);
    }


}
