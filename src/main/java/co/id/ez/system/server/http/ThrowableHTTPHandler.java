/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.id.ez.system.server.http;

import co.id.ez.system.core.log.LogService;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 *
 * @author Lutfi
 */
@Provider
public class ThrowableHTTPHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable e) {
        LogService
                .getInstance(this)
                .error()
                .withCause(e)
                .log("Error handle request", true);

        String tMsg = "Someting wrong with our system, "
                + "please contact us. <Error: " + e + ">";

        return Response.status(Response.Status.NOT_FOUND)
                .entity(tMsg)
                .type("text/plain")
                .build();
    }

}
