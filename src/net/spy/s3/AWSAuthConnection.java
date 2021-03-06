//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

package net.spy.s3;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An interface into the S3 system. It is initially configured with
 * authentication and connection parameters and exposes methods to access and
 * manipulate S3 data.
 */
public class AWSAuthConnection {

    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AWSAuthConnection.class.getName());
    
//    private HttpURLConnection connection;
    
	private String awsAccessKeyId;

	private String awsSecretAccessKey;

	private boolean isSecure;

	private String server;

	private int port;

	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey) {
		this(awsAccessKeyId, awsSecretAccessKey, true);
	}

	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey,
			boolean isSecure) {
		this(awsAccessKeyId, awsSecretAccessKey, isSecure, Utils.DEFAULT_HOST);
	}

	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey,
			boolean isSecure, String server) {
		this(awsAccessKeyId, awsSecretAccessKey, isSecure, server,
				isSecure ? Utils.SECURE_PORT : Utils.INSECURE_PORT);
	}

	/**
	 * Create a new interface to interact with S3 with the given credential and
	 * connection parameters
	 * 
	 * @param awsAccessKeyId
	 *            The your user key into AWS
	 * @param awsSecretAccessKey
	 *            The secret string used to generate signatures for
	 *            authentication.
	 * @param isSecure
	 *            True if the data should be encrypted on the wire on the way to
	 *            or from S3.
	 * @param server
	 *            Which host to connect to. Usually, this will be
	 *            s3.amazonaws.com
	 * @param port
	 *            Which port to use.
	 */
	public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey,
			boolean isSecure, String server, int port) {
		this.awsAccessKeyId = awsAccessKeyId;
		this.awsSecretAccessKey = awsSecretAccessKey;
		this.isSecure = isSecure;
		this.server = server;
		this.port = port;
	}

	/**
	 * Creates a new bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to create.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @param metadata
	 *            A Map of String to List of Strings representing the s3
	 *            metadata for this bucket (can be null).
	 */
	public CreateBucketResponse createBucket(String bucket,
			Map<String, List<String>> headers) throws MalformedURLException,
			IOException {
		return new CreateBucketResponse(makeRequest("PUT", bucket, headers));
	}

	/**
	 * Lists the contents of a bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to create.
	 * @param prefix
	 *            All returned keys will start with this string (can be null).
	 * @param marker
	 *            All returned keys will be lexographically greater than this
	 *            string (can be null).
	 * @param maxKeys
	 *            The maximum number of keys to return (can be null).
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public ListBucketResponse listBucket(String bucket, String prefix,
			String marker, Integer maxKeys, Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return listBucket(bucket, prefix, marker, maxKeys, null, headers);
	}

	/**
	 * Lists the contents of a bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to create.
	 * @param prefix
	 *            All returned keys will start with this string (can be null).
	 * @param marker
	 *            All returned keys will be lexographically greater than this
	 *            string (can be null).
	 * @param maxKeys
	 *            The maximum number of keys to return (can be null).
	 * @param delimiter
	 *            Keys that contain a string between the prefix and the first
	 *            occurrence of the delimiter will be rolled up into a single
	 *            element.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public ListBucketResponse listBucket(String bucket, String prefix,
			String marker, Integer maxKeys, String delimiter,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {

		String path = Utils.pathForListOptions(bucket, prefix, marker, maxKeys,
				delimiter);
		
//	    try {
//            String code = getS3ErrorCode(connection.getInputStream());
//            System.out.println("Error code is: " + code);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new IOException(e.getMessage());
//            
//        }
		return new ListBucketResponse(makeRequest("GET", path, headers));
	}

	/**
	 * Deletes a bucket.
	 * 
	 * @param bucket
	 *            The name of the bucket to delete.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public Response deleteBucket(String bucket,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new DeleteResponse(makeRequest("DELETE", bucket, headers));
	}

	/**
	 * Writes an object to S3.
	 * 
	 * @param bucket
	 *            The name of the bucket to which the object will be added.
	 * @param key
	 *            The name of the key to use.
	 * @param object
	 *            An S3Object containing the data to write.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public Response put(String bucket, String key, S3Object object,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
	    headers = new HashMap<String, List<String>>();
	    headers.put("Content-Length", java.util.Arrays.asList(String.valueOf(object.data.length)));
	    HttpURLConnection connection = makeRequest("PUT", bucket + "/"
				+ Utils.urlencode(key), headers, object);

		connection.setDoOutput(true);
		connection.getOutputStream().write(
				object.data == null ? new byte[] {} : object.data);
//		connection.getInputStream();
		System.out.println("URL:" + connection.getURL());
        System.out.println("Request Method:" + connection.getRequestMethod());
        System.out.println("Headers:");
        for (String mapKey: connection.getHeaderFields().keySet()) {
          System.out.println("\t" + mapKey + ": " + connection.getHeaderFields().get(mapKey));
        }
        System.out.println("Request Properties:");
        for (String requestKey: connection.getRequestProperties().keySet()) {
          System.out.println("\t" + requestKey + ": " + connection.getRequestProperties().get(requestKey));
        }
        
        System.out.println("Response code:" + connection.getResponseCode());
        System.out.println("Response message:" + connection.getResponseMessage());
        
		return new Response(connection);
	}
	
	public HttpURLConnection getOutputStream(String bucket, String key, S3Object object,
            Map<String, List<String>> headers, long length)
            throws MalformedURLException, IOException {
	    
	    headers = new HashMap<String, List<String>>();
	    headers.put("Expect", java.util.Arrays.asList("100-continue"));
	    headers.put("Content-Length", java.util.Arrays.asList(String.valueOf(length)));
	    headers.put("Content-Type",java.util.Arrays.asList("text/plain"));
	    HttpURLConnection connection = makeRequest("PUT", bucket + "/"
                + Utils.urlencode(key), headers, object);
	    
        connection.setDoOutput(true);
        
        return connection;
        
    }

	/**
	 * Reads an object from S3.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public GetResponse get(String bucket, String key,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new GetResponse(makeRequest("GET", bucket + "/"
				+ Utils.urlencode(key), headers));
	}
	
	public InputStream getInputStream(String bucket, String key,
            Map<String, List<String>> headers)
            throws MalformedURLException, IOException {
        return makeRequest("GET", bucket + "/"
                + Utils.urlencode(key), headers).getInputStream();
    }
	
	/**
	 * Deletes an object from S3.
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public Response delete(String bucket, String key,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new DeleteObjectResponse(makeRequest("DELETE", bucket + "/"
				+ Utils.urlencode(key), headers));
	}
	
	public CopyObjectResponse copy(String srcBucket, String srcKey, String destBucket, String destKey,
	        Map<String, List<String>> headers)
            throws MalformedURLException, IOException {
	    headers = new HashMap<String, List<String>>();
	    headers.put("x-amz-copy-source",java.util.Arrays.asList(srcBucket + "/" + srcKey));
	    
	    return new CopyObjectResponse(makeRequest("PUT", destBucket + "/"
                + Utils.urlencode(destKey), headers));
	}

	/**
	 * Get the logging xml document for a given bucket
	 * 
	 * @param bucket
	 *            The name of the bucket
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public GetResponse getBucketLogging(String bucket,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new GetResponse(makeRequest("GET", bucket + "?logging", headers));
	}

	/**
	 * Write a new logging xml document for a given bucket
	 * 
	 * @param loggingXMLDoc
	 *            The xml representation of the logging configuration as a
	 *            String
	 * @param bucket
	 *            The name of the bucket
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public Response putBucketLogging(String bucket, String loggingXMLDoc,
			Map<String, List<String>> headers)
		throws MalformedURLException, IOException {
		S3Object object = new S3Object(loggingXMLDoc.getBytes(), null);
		HttpURLConnection request = makeRequest("PUT", bucket + "?logging",
				headers, object);

		request.setDoOutput(true);
		request.getOutputStream().write(
				object.data == null ? new byte[] {} : object.data);

		return new Response(request);
	}

	/**
	 * Get the ACL for a given bucket
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public GetResponse getBucketACL(String bucket,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return getACL(bucket, "", headers);
	}

	/**
	 * Get the ACL for a given object (or bucket, if key is null).
	 * 
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public GetResponse getACL(String bucket, String key,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		if (key == null) {
			key = "";
		}
		return new GetResponse(makeRequest("GET", bucket + "/"
				+ Utils.urlencode(key) + "?acl", headers));
	}

	/**
	 * Write a new ACL for a given bucket
	 * 
	 * @param aclXMLDoc
	 *            The xml representation of the ACL as a String
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public Response putBucketACL(String bucket, String aclXMLDoc,
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return putACL(bucket, "", aclXMLDoc, headers);
	}

	/**
	 * Write a new ACL for a given object
	 * 
	 * @param aclXMLDoc
	 *            The xml representation of the ACL as a String
	 * @param bucket
	 *            The name of the bucket where the object lives.
	 * @param key
	 *            The name of the key to use.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public Response putACL(String bucket, String key, String aclXMLDoc,
			Map<String, List<String>> headers)
		throws MalformedURLException, IOException {
		S3Object object = new S3Object(aclXMLDoc.getBytes(), null);

		HttpURLConnection request = makeRequest("PUT", bucket + "/"
				+ Utils.urlencode(key) + "?acl", headers, object);

		request.setDoOutput(true);
		request.getOutputStream().write(
				object.data == null ? new byte[] {} : object.data);

		return new Response(request);
	}

	/**
	 * List all the buckets created by this account.
	 * 
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	public ListAllMyBucketsResponse listAllMyBuckets(
			Map<String, List<String>> headers)
			throws MalformedURLException, IOException {
		return new ListAllMyBucketsResponse(makeRequest("GET", "", headers));
	}
	
	public void kill() throws IOException {
//	    connection.getOutputStream().close();
//	    connection.getInputStream().close();
//	    connection.disconnect();
	}

	/**
	 * Make a new HttpURLConnection without passing an S3Object parameter.
	 */
	private HttpURLConnection makeRequest(String method, String resource,
			Map<String, List<String>> headers)
	throws MalformedURLException, IOException {
		return makeRequest(method, resource, headers, null);
	}
	/**
	 * Make a new HttpURLConnection.
	 * 
	 * @param method
	 *            The HTTP method to use (GET, PUT, DELETE)
	 * @param resource
	 *            The resource name (bucketName + "/" + key).
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @param object
	 *            The S3Object that is to be written (can be null).
	 */
	private HttpURLConnection makeRequest(String method, String resource,
			Map<String, List<String>> headers, S3Object object)
	throws MalformedURLException,
			IOException {
		URL url = makeURL(resource);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);

		addHeaders(connection, headers);
		if (object != null) {
			addMetadataHeaders(connection, object.metadata);
		}
		addAuthHeader(connection, method, resource);

		for (String key: connection.getRequestProperties().keySet()) {
            System.out.println(key + ": " + connection.getRequestProperties().get(key));
        }
//		System.out.println("Headers:");
//        for (String mapKey: connection.getHeaderFields().keySet()) {
//          System.out.println("\t" + mapKey + ": " + connection.getHeaderFields().get(mapKey));
//        }
		
		return connection;
	}

	/**
	 * Add the given headers to the HttpURLConnection.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the headers will be added.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 */
	private void addHeaders(HttpURLConnection connection,
			Map<String, List<String>> headers) {
		addHeaders(connection, headers, "");
	}

	/**
	 * Add the given metadata fields to the HttpURLConnection.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the headers will be added.
	 * @param metadata
	 *            A Map of String to List of Strings representing the s3
	 *            metadata for this resource.
	 */
	private void addMetadataHeaders(HttpURLConnection connection,
			Map<String, List<String>> metadata) {
		addHeaders(connection, metadata, Utils.METADATA_PREFIX);
	}

	/**
	 * Add the given headers to the HttpURLConnection with a prefix before the
	 * keys.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the headers will be added.
	 * @param headers
	 *            A Map of String to List of Strings representing the http
	 *            headers to pass (can be null).
	 * @param prefix
	 *            The string to prepend to each key before adding it to the
	 *            connection.
	 */
	private void addHeaders(HttpURLConnection connection,
			Map<String, List<String>> headers, String prefix) {
		if (headers != null) {
			for(Map.Entry<String, List<String>> me : headers.entrySet()) {
				String key=me.getKey();
				for(String value : me.getValue()) {
					connection.addRequestProperty(prefix + key, value);
				}
			}
		}
	}

	/**
	 * Add the appropriate Authorization header to the HttpURLConnection.
	 * 
	 * @param connection
	 *            The HttpURLConnection to which the header will be added.
	 * @param method
	 *            The HTTP method to use (GET, PUT, DELETE)
	 * @param resource
	 *            The resource name (bucketName + "/" + key).
	 */
	private void addAuthHeader(HttpURLConnection connection, String method,
			String resource) {
		if (connection.getRequestProperty("Date") == null) {
			connection.setRequestProperty("Date", httpDate());
		}
		if (connection.getRequestProperty("Content-Type") == null) {
			connection.setRequestProperty("Content-Type", "");
		}
		//if (connection.getRequestProperty("Host") == null) {
        //    connection.setRequestProperty("Host", (resource.equals("")?this.server:resource+"." + this.server));
       // }
		String canonicalString = Utils.makeCanonicalString(method, resource,
				connection.getRequestProperties());
		String encodedCanonical = Utils.encode(this.awsSecretAccessKey,
				canonicalString, false);
		connection.setRequestProperty("Authorization", "AWS "
				+ this.awsAccessKeyId + ":" + encodedCanonical);
		
	}

	/**
	 * Create a new URL object for a given resource.
	 * 
	 * @param resource
	 *            The resource name (bucketName + "/" + key).
	 */
	private URL makeURL(String resource) throws MalformedURLException {
		String protocol = this.isSecure ? "https" : "http";
		return new URL(protocol, this.server,this.port,"/" + resource);
	}

	/**
	 * Generate an rfc822 date for use in the Date HTTP header.
	 */
	public static String httpDate() {
		final String DateFormat = "EEE, dd MMM yyyy HH:mm:ss ";
		SimpleDateFormat format = new SimpleDateFormat(DateFormat, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(new Date()) + "GMT";
	}
	
	public String getS3ErrorCode(InputStream doc) throws Exception
    {
      String code = null;
      SAXParserFactory parserfactory = SAXParserFactory.newInstance();
      parserfactory.setNamespaceAware(false);
      parserfactory.setValidating(false);
      SAXParser xmlparser = parserfactory.newSAXParser();
      S3ErrorHandler handler = new S3ErrorHandler();
      xmlparser.parse(doc, handler);
      code = handler.getErrorCode();
      return code;
    }

    // This inner class implements a SAX handler.
    class S3ErrorHandler extends DefaultHandler
    {
      private StringBuffer code = new StringBuffer();
      private boolean append = false;

      public void startElement(String uri, String ln, String qn, Attributes atts)
      {
        if (qn.equalsIgnoreCase("Code")) append = true;
      }
      public void endElement(String url, String ln, String qn)
      {
        if (qn.equalsIgnoreCase("Code")) append = false;
      }
      public void characters(char[] ch, int s, int length)
      {
        if (append) code.append(new String(ch, s, length));
      }

      public String getErrorCode()
      {
        return code.toString();
      }
    }
}
