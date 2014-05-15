package com.hp.it.llba.services;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.hp.it.llba.web.WebappContext;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Returns a PDF content to be displayed on the web page.
 * @author mingxin.he@hp.com
 */
@Path("/")
@Service
public class PDFViewerRESTService {

    private static final int MAX_NAME_LENGTH= 150;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PDFViewerRESTService.class.getName());

    public PDFViewerRESTService() throws Exception {
    }

    @POST
    @Path("/PostDOCViewerRESTService")
    //@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response viewDocumentFile(@FormDataParam("file") InputStream inputStream,
                                     @FormDataParam("file") FormDataContentDisposition fileDetail,
                                     @Context ServletContext context,
                                     @Context HttpServletRequest request) throws Exception
    {
        File content = null;
        Map<String, String> responseObj = new HashMap<String, String>();
        String fullName = fileDetail.getFileName();

        String inputExtension = FilenameUtils.getExtension(fullName);

        String baseName = String.valueOf(Math.abs(new Random().nextInt()));  //FilenameUtils.getBaseName(fullName).replaceAll(" ", "");

        InputStream input = new BufferedInputStream(inputStream);

        // cut the file name if name is too long, because OS allows only file with name < 250 characters
        baseName = reduceFileNameSize(baseName);
        content = File.createTempFile(baseName + "_tmp", ".pdf", new File(context.getRealPath(File.separator) + "/web/tmp"));
        // Convert to pdf if need
        if (inputExtension.equals("pdf")) {
            read(input, new BufferedOutputStream(new FileOutputStream(content)));
        } else {
            // create temp file to store original data of nt:file node

            File in = File.createTempFile(baseName + "_tmp", null);
            read(input, new BufferedOutputStream(new FileOutputStream(in)));
            try {
                OfficeDocumentConverter documentConverter = WebappContext.getDocumentConverter();
                OfficeManager officeManager = WebappContext.getOfficeManager();

                if (officeManager != null && officeManager.isRunning()) {
                    if (documentConverter != null) {

                        documentConverter.convert(in, content);
                    }
                    else
                    {
                        content.delete();
                    }
                }
                else
                {
                    LOG.warn("this OfficeManager is currently stopped!");
                    responseObj.put("error", "this OfficeManager is currently stopped");
                }

            } catch (OfficeException connection) {
                content.delete();
                responseObj.put("error", "Exception when using Office Service");
                if (LOG.isErrorEnabled()) {
                    LOG.error("Exception when using Office Service");
                }
            } finally {
                in.delete();
            }
        }

        URL location = new URL(request.getScheme(),
                request.getServerName(),
                request.getServerPort(), context.getContextPath() + "/web/viewer.html?file=tmp/" + content.getName());


        responseObj.put("URL", location.toString());
        return Response.status(Response.Status.OK).entity(responseObj).build();
    }

    @GET
    @Path("/GetDOCViewerRESTService")
    //@Consumes(MediaType.APPLICATION_JSON)
    @Produces("pdf/*")
    //@Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response getPDFDocumentFile(@QueryParam("reference") String reference,
                                       @QueryParam("OutputType") String OutputType, @Context HttpServletRequest request) throws Exception
    {
        LOG.debug("RequestURL:" + request.getRequestURL().toString() + "?" + request.getQueryString().toString());
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        reference = reference.replaceAll(" ", "%20");
        URL httpurl = new URL(reference);
        String fullName = getFileNameFromUrl(reference);
        String inputExtension = FilenameUtils.getExtension(fullName);
        //String baseName = FilenameUtils.getBaseName(fullName).replaceAll(" ", "");
        String baseName = String.valueOf(Math.abs(new Random().nextInt()));
        baseName = reduceFileNameSize(baseName);

        /*
        Authenticator.setDefault (new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication ("mingxin.he@hp.com", "Pass5%word1234".toCharArray());
            }
        });
        */

        // cut the file name if name is too long, because OS allows only file with name < 250 characters

        File content = File.createTempFile(baseName + "_tmp", ".pdf", new File(request.getRealPath(File.separator) + "/web/tmp"));
        if (inputExtension != null && inputExtension.equals("pdf")) {
            FileUtils.copyURLToFile(httpurl, content);
        } else {
            File in = File.createTempFile(baseName + "_tmp", null);
            FileUtils.copyURLToFile(httpurl, in);
            try {
                OfficeDocumentConverter documentConverter = WebappContext.getDocumentConverter();
                OfficeManager officeManager = WebappContext.getOfficeManager();

                if (officeManager != null && officeManager.isRunning()) {
                    if (documentConverter != null) {
                        DocumentFormat outputFormat = documentConverter.getFormatRegistry().getFormatByExtension("pdf");
                        documentConverter.convert(in, content);
                    } else {
                        content.delete();
                    }
                } else {
                    LOG.warn("this OfficeManager is currently stopped!");
                }

            } catch (OfficeException connection) {
                content.delete();
                if (LOG.isErrorEnabled()) {
                    LOG.error("Exception when using Office Service");
                }
            } finally {
                in.delete();
            }
        }

        if (OutputType.equals("pdf")) {
            String mt = new MimetypesFileTypeMap().getContentType(content);
            return Response.ok(content, mt).header("Content-disposition", "attachment;filename=" + content.getName()).header("ragma", "No-cache").header("Cache-Control", "no-cache").build();
        }
        else if (OutputType.equals("html")) {
            java.net.URI location = new java.net.URI("../web/viewer.html?file=tmp/" + content.getName());

            return Response.temporaryRedirect(location).build();
        }
        else return null;
    }

    private void read(InputStream is, OutputStream os) throws Exception {
        int bufferLength = 1024;
        int readLength = 0;
        while (readLength > -1) {
            byte[] chunk = new byte[bufferLength];
            readLength = is.read(chunk);
            if (readLength > 0) {
                os.write(chunk, 0, readLength);
            }
        }
        os.flush();
        os.close();
    }

    private static String getFileNameFromUrl(String url){

        String name = new Long(System.currentTimeMillis()).toString() + ".x";

        int index = url.lastIndexOf("/");

        if(index > 0){

            name = url.substring(index + 1);

            if(name.trim().length()>0){

                return name;

            }

        }

        return name;

    }

    /**
     * reduces the file name size. If the length is > 150, return the first 150 characters, else, return the original value
     * @param name the name
     * @return the reduced name
     */
    private String reduceFileNameSize(String name) {
        return (name != null && name.length() > MAX_NAME_LENGTH) ? name.substring(0, MAX_NAME_LENGTH) : name;
    }
}