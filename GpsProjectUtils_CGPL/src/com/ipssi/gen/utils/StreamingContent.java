package com.ipssi.gen.utils;

/**
 * Title:        StreamingContent<p>
 * Description:  This servlet handles streaming both  binary as well as character content 
 * example : PDF, xml, html, image, audio, video etc.
 * Input this class: <p>
 * Copyright:    <p>
 * Company:      <p>
 * @author       Raj Behera
 */
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;
/**
 * <p> This class handles Streaming Data Content
**/
public class StreamingContent extends HttpServlet 
{
 
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
  }
  
/*
 * This Method Handles Post
 * <p>
 * @param HttpServletRequest request
 * @param HttpServletResponse response
 */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    doGet(request,response);
  }

/*
 * This Method Handles Get
 * <p>
 * @param HttpServletRequest request
 * @param HttpServletResponse response
 */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String urlstr = null;
    String format = null;  
    try {
      
      response.reset();
      urlstr =  request.getParameter("url");//this url is the source of data
      format = request.getParameter("format");//this is the content out type
      //Stream both character and binary data
      if(format.equalsIgnoreCase("xml") || format.equalsIgnoreCase("html"))
      {
        PrintWriter pWriter = response.getWriter();//this is for character data
        streamCharacterData(urlstr,format,pWriter,response);//for character data
      }
      else
      {
        ServletOutputStream sOutStream = response.getOutputStream();//this is for binary data
        streamBinaryData(urlstr,format,sOutStream,response);//for binary data
      }
      
    } catch (Exception e) {
        e.printStackTrace();
    } 
  }
    
/*
 * This Method Handles streaming Binary data
 * <p>
 * @param String urlstr ex: http;//localhost/test.pdf etc.
 * @param String format ex: pdf or audio_wav or msdocuments etc.
 * @param ServletOutputStream outstr
 * @param HttpServletResponse resp
 */
  private void streamBinaryData(String urlstr,String format,ServletOutputStream outstr, HttpServletResponse resp)
  {
        String ErrorStr = null;
        try{
          //find the right mime type and set it as contenttype
          resp.setContentType(getMimeType(format));
          BufferedInputStream bis = null;
          BufferedOutputStream bos = null;          
          try{
              URL url	= new URL(urlstr);
              URLConnection urlc= url.openConnection();
              int length = urlc.getContentLength();
              resp.setContentLength(length); 
              // Use Buffered Stream for reading/writing.
              InputStream in = urlc.getInputStream();
              bis = new BufferedInputStream(in);
              bos = new BufferedOutputStream(outstr);
              byte[] buff = new byte[length];
              int bytesRead;
              // Simple read/write loop.
              while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
              }
          } catch (Exception e) {
                e.printStackTrace();
                ErrorStr = "Error Streaming the Data";
                outstr.print(ErrorStr);
          } finally {
                if( bis != null ) {
                  bis.close();
                }
                if( bos != null ) {
                  bos.close();
                }
                if( outstr != null ) {
                  outstr.flush();
                  outstr.close();
                }
          }  
        }
        catch(Exception e){
                e.printStackTrace();
        }
  }

/*
 * This Method Handles streaming Character data
 * <p>
 * @param String urlstr ex: http;//localhost/test.pdf etc.
 * @param String format ex: xml or html etc.
 * @param PrintWriter outstr
 * @param HttpServletResponse resp
 */
  private void streamCharacterData(String urlstr,String format,PrintWriter outstr, HttpServletResponse resp)
  {
        String ErrorStr = null;
        try{
          //find the right mime type and set it as contenttype
          resp.setContentType(getMimeType(format));
          InputStream in = null;       
          try{
              URL url	= new URL(urlstr);
              URLConnection urlc= url.openConnection();
              int length = urlc.getContentLength();
              in = urlc.getInputStream();  
              resp.setContentLength(length);
              int ch;
              while ( (ch = in.read()) != -1 ) {
                outstr.print( (char)ch );
              }
          } catch (Exception e) {
                e.printStackTrace();
                ErrorStr = "Error Streaming the Data";
                outstr.print(ErrorStr);
          } finally {
                if( in != null ) {
                  in.close();
                }
                if( outstr != null ) {
                  outstr.flush();
                  outstr.close();
                }
          }  
        }
        catch(Exception e){
                e.printStackTrace();
        }
  }

/*
 * This Method Returns the right mime type for a particular  format
 * <p>
 * @param String format ex: xml or html etc.
 * @return String mimetype
 */
    private String getMimeType(String format)
    {
          if(format.equalsIgnoreCase("pdf")) //check the out type
              return "application/pdf";
          else if(format.equalsIgnoreCase("audio_basic"))
              return "audio/basic";
          else if(format.equalsIgnoreCase("audio_wav"))
              return "audio/wav";
          else if(format.equalsIgnoreCase("image_gif"))
              return "image/gif";
          else if(format.equalsIgnoreCase("image_jpeg"))
              return "image/jpeg";
          else if(format.equalsIgnoreCase("image_bmp"))
              return "image/bmp";
          else if(format.equalsIgnoreCase("image_x-png"))
              return "image/x-png";
          else if(format.equalsIgnoreCase("msdownload"))
              return "application/x-msdownload";
          else if(format.equalsIgnoreCase("video_avi"))
              return "video/avi";
          else if(format.equalsIgnoreCase("video_mpeg"))
              return "video/mpeg";
          else if(format.equalsIgnoreCase("html"))
              return "text/html";
          else if(format.equalsIgnoreCase("xml"))
              return "text/xml";
          else
              return null;
    }

}