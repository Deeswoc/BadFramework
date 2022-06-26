package utils.VeryBadHTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class Request {

    private METHOD method;
    private String url;
    private String contentType;
    int contentLength;

    HashMap<String, String> cookies;

    public METHOD getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public Request(InputStream inputStream) throws BadRequestException {
        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            /// Getting Request Type
            String startLine;
            startLine = bufferedReader.readLine();
            String[] request = startLine.split(" ");
            if ((request.length != 3 && request.length != 2) || request[1].charAt(0) != '/')
                throw new BadRequestException("Error Parsing Request");
            switch (request[0]) {
                case "POST" -> method = METHOD.POST;
                case "GET" -> method = METHOD.GET;
                case "PUT" -> method = METHOD.PUT;
                case "DELETE" -> method = METHOD.DELETE;
                default -> throw new BadRequestException("Invalid Method");
            }


            this.url = request[1];

            /// Reading Headers
            while (startLine != null && !startLine.equals("")) {
                startLine = bufferedReader.readLine();
                if (startLine != null) {
                    String[] header = startLine.split(":");
                    switch (header[0].trim()) {
                        case "Cookie" -> {
                            cookies = new HashMap<>();

                            for (String cookie : header[1].split(";")) {
                                String[] keyVal = cookie.split("=");
                                if (keyVal.length == 2) {
                                    cookies.put(keyVal[0], keyVal[1]);
                                }
                            }
                        }
                        case "Content-Type" -> this.contentType = header[1];
                        case "Content-Length" -> {
                            try {
                                this.contentLength = parseInt(header[1]);
                            } catch (NumberFormatException ex) {
                                this.contentLength = -1;
                            }
                        }
                    }
                }
            }

            String line = bufferedReader.readLine();
            if (line.equals("")) {
                do {
                    line = bufferedReader.readLine();
                } while (!line.equals(""));
            }
            /// Reading body


        } catch (IOException ex) {
            System.out.println("Error sending response");
            ex.printStackTrace();
        }
    }
}
