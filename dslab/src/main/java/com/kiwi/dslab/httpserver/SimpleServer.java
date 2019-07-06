package com.kiwi.dslab.httpserver;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;

public class SimpleServer extends NanoHTTPD {
    public SimpleServer() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);  // daemon true!
        System.out.println("Running!");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> parms = session.getParms();
        String msg = "Hello, ";
        if (parms.get("username") == null) {
            msg += "visitor!";
        } else {
            msg += parms.get("username");
        }
        msg += " uri: " + uri;
        return newFixedLengthResponse(msg);
    }

    public static void main(String[] args) throws IOException {
        new SimpleServer();
    }
}
