package com.github.cliveevans.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class WebResponseTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private AsyncResponse async;

    @Mock
    private AsyncResult<HttpResponse<Buffer>> result;

    @Mock
    private HttpResponse<Buffer> response;

    @Mock
    private Handler<HttpResponse<Buffer>> successHandler;

    private JsonObject jsonObject = new JsonObject().put("some", "data");

    private WebResponse webResponse = new WebResponse();

    @Test
    public void simpleCaseShouldCallResumeOnAsyncObjectIfSuccessful() throws Exception {
        int status = 200;
        successfullyRespondWith(status, jsonObject);

        webResponse.handler(async).handle(result);

        verify(async).resume(responseFor(status, jsonObject));
    }

    @Test
    public void simpleCaseShouldBadGatewayOnRemote4xxError() throws Exception {
        int status = 404;
        successfullyRespondWith(status, "whoops");

        webResponse.handler(async).handle(result);

        JsonObject errorObject = new JsonObject().put("remote_status", status).put("remote_error", "whoops");
        verify(async).resume(responseFor(502, errorObject));
    }

    @Test
    public void simpleCaseShouldBadGatewayOnRemote3xxError() throws Exception {
        int status = 302;
        successfullyRespondWith(status, "whoops");

        webResponse.handler(async).handle(result);

        JsonObject errorObject = new JsonObject().put("remote_status", status).put("remote_error", "whoops");
        verify(async).resume(responseFor(502, errorObject));
    }

    @Test
    public void simpleHandlerShouldReturn500WhenThereIsAProblemWithTheRemote() throws Exception {
        WebResponse.WebResponseHandler simpleHandler = webResponse.handler(async);

        RuntimeException exception = new RuntimeException();
        when(result.succeeded()).thenReturn(false);
        when(result.cause()).thenReturn(exception);

        simpleHandler.handle(result);

        verify(async).resume(responseFor(500, exception));
    }

    @Test
    public void customSuccessHandlerShouldBeCalledOnSuccessfulCall() throws Exception {
        successfullyRespond(200);

        webResponse.handler(successHandler, async).handle(result);

        verify(successHandler).handle(response);
        verifyNoMoreInteractions(async);
    }

    private void successfullyRespond(int status) {
        when(result.succeeded()).thenReturn(true);
        when(result.result()).thenReturn(response);
        when(response.statusCode()).thenReturn(status);
    }

    private void successfullyRespondWith(int status, JsonObject actual) {
        successfullyRespond(status);
        when(response.bodyAsJsonObject()).thenReturn(actual);
    }

    private void successfullyRespondWith(int status, String actual) {
        successfullyRespond(status);
        when(response.bodyAsString()).thenReturn(actual);
    }

    private Response responseFor(int status, Object expected) {
        return argThat(new ArgumentMatcher<Response>() {
            @Override
            public boolean matches(Response response) {
                return response.getStatus() == status && expected.equals(response.getEntity());
            }

            @Override
            public String toString() {
                return String.format("Status: %d and entity %s", status, expected);
            }
        });
    }
}