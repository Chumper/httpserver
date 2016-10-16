package integration;

import com.oracle.tools.packager.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import integration.util.ServerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommentPostTest extends ServerTest {

  @Test
  public void testCommentPost() throws IOException {

    Response response = Request.Post(serverUrl + "comments")
        .bodyForm(
            Arrays.asList(
                new BasicNameValuePair("name", "John"),
                new BasicNameValuePair("message", "Doe"),
                new BasicNameValuePair("add", "Submit")
            )
        )
        .execute();

    HttpResponse resp = response.returnResponse();

    assertEquals(200, resp.getStatusLine().getStatusCode());
    String content = EntityUtils.toString(resp.getEntity(), "UTF-8");
    assertTrue(content.contains("name: John"));
    assertTrue(content.contains("message: Doe"));
  }

}
