import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Server {
    ServerSocket serverSocket;

    Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Method for start server and listen for clients
     */
    public void startServer() {
        try {
            // getting custom cursor image from resources
            Image cursor = ImageIO.read(Objects.requireNonNull(getClass().getResource("cursor.png")));

            //listening for clients
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                System.out.println("Client connected");

                //start new thread for every client connect
                new Thread(() -> {
                    while (socket.isConnected()) {
                        try {
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                            //capture screen
                            BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

                            //draw custom cursor on screen captured image
                            Graphics2D graphics2D = image.createGraphics();
                            int mouseX = MouseInfo.getPointerInfo().getLocation().x;
                            int mouseY = MouseInfo.getPointerInfo().getLocation().y;
                            graphics2D.drawImage(cursor, mouseX, mouseY, null);
                            graphics2D.dispose();

                            //convert image to byte array
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                            //compressing image
                            image = compress(image, 0.1f);

                            //write image to byte array
                            ImageIO.write(image, "jpg", byteArrayOutputStream);

                            //send image size to client
                            dataOutputStream.writeInt(byteArrayOutputStream.size());

                            //send image to client
                            dataOutputStream.write(byteArrayOutputStream.toByteArray());
                            dataOutputStream.flush();

                            Thread.sleep(1);
                        } catch (Exception e) {
                            disconnectClient(socket);
                            break;
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println("Server closed");
        }
    }

    /**
     * Method for compressing image
     *
     * @param image image to compress
     * @param comp  quality of compression
     * @return compressed image
     * @throws IOException if image is not found
     */
    public static BufferedImage compress(BufferedImage image, float comp) throws IOException {
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(comp);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageOutputStream stream = ImageIO.createImageOutputStream(outputStream);
        jpgWriter.setOutput(stream);
        jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);

        byte[] bytes = outputStream.toByteArray();
        stream.close();

        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    /**
     * Method for disconnect client
     *
     * @param socket socket of client
     */
    public static void disconnectClient(Socket socket) {
        System.out.println("Client disconnected");
        try {
            socket.close();
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            Server server = new Server(serverSocket);

            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server is running on " + ip);

            server.startServer();

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
