package CaptureImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import com.ipssi.gen.utils.DBConnectionPool;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class ImageCapture {

	static {
		String path = null;
		String path2 = null;
		try {
			// I have copied dlls from opencv folder to my project folder
			// path="C:\\WorkSpaceCGPLOxygen\\JavaExamples2";
			path = "C:\\Users\\IPSSI\\Desktop\\ImageCapture\\opencv3.46\\opencv\\build\\java\\x64";
			path2 = "C:\\Users\\IPSSI\\Desktop\\ImageCapture\\opencv3.46\\opencv\\build\\bin";
			System.load(path + "\\opencv_java346.dll");
			System.load(path2 + "\\opencv_ffmpeg346_64.dll");

			// System.out.println(System.getProperty("java.library.path"));
			// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Error loading libs");
		}
	}

	public static void main(String[] args) {
		try {
			ImageCapture app = new ImageCapture();
			// Address can be different. Check your cameras manual. :554 a standard RTSP
			// port for cameras but it can be different
			String addressString = "rtsp://admin:admin123@192.168.1.89:554/cam/realmonitor?channel=1&subtype=0";
			Mat mat = new Mat();
			// VideoCapture capturedVideo = new VideoCapture(addressString);
			// boolean isOpened = capturedVideo.open(addressString);
			VideoCapture capturedVideo = new VideoCapture(0);
			boolean isOpened = capturedVideo.open(0);
			byte[] data = app.openRTSP(isOpened, capturedVideo, mat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] openRTSP(boolean isOpened, VideoCapture capturedVideo, Mat cameraMat) throws Exception {
		InputStream is = null;
		byte[] data = null;
		Imgcodecs imageCodecs = new Imgcodecs();
		String file = "D:\\Admit\\test.jpg";
		if (isOpened) {
			boolean tempBool = capturedVideo.read(cameraMat);
			System.out.println("VideoCapture returned mat? " + tempBool);
			
			if (!cameraMat.empty()) {
				System.out.println("Print image size: " + cameraMat.size());
//				boolean isCaptured = imageCodecs.imwrite(file, cameraMat);
//				System.out.println(isCaptured);
				data = new byte[cameraMat.channels() * cameraMat.cols() * cameraMat.rows()];
				BufferedImage originalImage = ImageIO.read(new File(file));
				BufferedImage image = new BufferedImage(cameraMat.width(), cameraMat.height(),BufferedImage.TYPE_3BYTE_BGR);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write( originalImage, "jpg", baos );
				byte[] imageInByte = baos.toByteArray();
				insertImageDB(1,imageInByte);
				baos.flush();
				baos.close();
			} else {
				System.out.println("Mat is empty.");
			}
		} else {
			System.out.println("Camera connection problem. Check addressString");
		}
		return data;
	}

	private void insertImageDB(int i, byte[] data) {
		Connection conn = null;
		PreparedStatement ps=null;
//		String q= "insert into tp_step (image_1) values (?)";
		String q= "update tp_step set image_1=? where tps_id=1";
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps=conn.prepareStatement(q);
			ps.setBytes(1, data);
			ps.execute();
			System.out.println(ps.toString());
			conn.commit();
		}catch(Exception e) {
			
		} finally {

			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	public int safeLongToInt(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            (l + " cannot be cast to int without changing its value.");
	    }
	    return (int) l;
	}
}