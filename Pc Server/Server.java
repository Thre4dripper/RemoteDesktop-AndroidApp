import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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

                            //write image to byte array
                            ImageIO.write(image, "png", byteArrayOutputStream);

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
