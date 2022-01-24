package CaptureImage;

import java.io.IOException;
import java.net.ServerSocket;

public class captureService {
		   public static void main(String[] args) {
		      try {
		         int i = 1;
		         ServerSocket s = new ServerSocket(8189);

		         while(true) {
//		            Runnable r = new ThreadedEchoHandler(incoming, i);
//		            Thread t = new Thread(r);
//		            t.start();
		            i++;
		         }
		      } catch (IOException e) {
		         e.printStackTrace();
		      }
		   }
		}
