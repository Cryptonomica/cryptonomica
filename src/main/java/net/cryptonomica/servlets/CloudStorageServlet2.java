package net.cryptonomica.servlets;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.*;
import com.google.gson.Gson;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.logging.Logger;

/**
 * A simple servlet that proxies reads and writes to its Google Cloud Storage bucket.
 */
@SuppressWarnings("serial")
public class CloudStorageServlet2 extends HttpServlet {

    /* --- Logger: */
    private final static Logger LOG = Logger.getLogger(CloudStorageServlet2.class.getName());
    /* --- Gson*/
    private final static Gson GSON = new Gson();

    public static final boolean SERVE_USING_BLOBSTORE_API = false;

    /**
     * This is where backoff parameters are configured. Here it is aggressively retrying with
     * backoff, up to 10 times but taking no more that 15 seconds total to do so.
     */
    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());

    /**
     * Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB
     */
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    /**
     * Retrieves a file from GCS and returns it in the http response.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to read the GCS file named Bar in the bucket Foo.
     */
//[START doGet]
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GcsFilename fileName = getFileName(req);
        if (SERVE_USING_BLOBSTORE_API) {
            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
            BlobKey blobKey = blobstoreService.createGsBlobKey(
                    "/gs/" + fileName.getBucketName() + "/" + fileName.getObjectName());
            blobstoreService.serve(blobKey, resp);
        } else {
            GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, BUFFER_SIZE);
            copy(Channels.newInputStream(readChannel), resp.getOutputStream());
        }
    }

    /**
     * Writes the payload of the incoming post as the contents of a file to GCS.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to create a GCS file named Bar in bucket Foo.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
        LOG.warning("GcsFileOptions.getDefaultInstance(): " + instance.toString());
        GcsFilename fileName = getFileName(req);
        LOG.warning("GcsFilename: " + fileName.toString());
        GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, instance);
        LOG.warning("[GcsOutputChannel]" + outputChannel.toString()
                + "filename: " + outputChannel.getFilename()
                + ", BufferSizeBytes: " + outputChannel.getBufferSizeBytes()
                + ", isOpen()" + outputChannel.isOpen()
        );
        // outputChannel.waitForOutstandingWrites(); // ?

        // Creates a MultipartParser from the specified request, which limits the upload size to the specified length,
        // buffers for performance and prevent attempts to read past the amount specified by the Content-Length.
        // see: http://www.servlets.com/cos/javadoc/com/oreilly/servlet/multipart/MultipartParser.html
        int size = 209715200; // 200MB in bytes
        MultipartParser multipartParser = new MultipartParser(req, size);
        com.oreilly.servlet.multipart.Part part;
        FilePart filePart = null;
        int numberOfBytesRead = 0;
        while (true) {
            part = multipartParser.readNextPart();
            if (part == null) {
                break;
            } else if (part.isFile()) {
                // http://www.servlets.com/cos/javadoc/com/oreilly/servlet/multipart/FilePart.html
                filePart = (FilePart) part;
                LOG.warning("got filePart: "
                        + filePart.toString()
                        + ", file name: " + filePart.getFileName()
                        + ", content type: " + filePart.getContentType()
                        + ", getName(): " + filePart.getName()
                );
                //
                // Returns an input stream which contains the contents of the file supplied.
                // If the user didn't enter a file to upload there will be 0 bytes in the input stream.
                // !!!!>>> It's important to read the contents of the InputStream immediately and in full
                // before proceeding to process the next part.
                // The contents will otherwise be lost on moving to the next part.
                // in = filePart.getInputStream(); // does not work!!!
                numberOfBytesRead = copy(filePart.getInputStream(), Channels.newOutputStream(outputChannel));

            }
        } // end of while

        // copy(req.getInputStream(), Channels.newOutputStream(outputChannel));

        FileUploadResponse fileUploadResponse = new FileUploadResponse(numberOfBytesRead, "message");

        ServletUtils.sendJsonResponse(resp, GSON.toJson(fileUploadResponse));

    } // end of doPost

    private GcsFilename getFileName(HttpServletRequest req) {
        String[] splits = req.getRequestURI().split("/", 4);
        if (!splits[0].equals("") || !splits[1].equals("gcs")) {
            throw new IllegalArgumentException("The URL is not formed as expected. " +
                    "Expecting /gcs/<bucket>/<object>");
        }
        return new GcsFilename(splits[2], splits[3]);
    }

    /**
     * Transfer the data from the inputStream to the outputStream. Then close both streams.
     */
    private int copy(InputStream input, OutputStream output) throws IOException {
        int bytesReadCounter = 0;
        LOG.warning("[copy(InputStream input, OutputStream output)]");
        // Returns an estimate of the number of bytes that can be read (or
        //         * skipped over) from this input stream without blocking by the next
        //         * invocation of a method for this input stream. The next invocation
        //         * might be the same thread or another thread.  A single read or skip of this
        //         * many bytes will not block, but may read or skip fewer bytes.
        LOG.warning("input.available(): " + input.available());
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            bytesReadCounter = bytesReadCounter + bytesRead;
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
        return bytesReadCounter;
    } // end of copy

    private class FileUploadResponse {
        long numberOfBytesWritten;
        String message;

        public FileUploadResponse(long numberOfBytesWritten, String message) {
            this.numberOfBytesWritten = numberOfBytesWritten;
            this.message = message;
        }
    }
}
