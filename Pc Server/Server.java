import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Server {
    ServerSocket serverSocket;
    static int x;
    static int y;

    Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new Thread(() -> {
                    while (socket.isConnected()) {
                        try {
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            Robot robot = new Robot();
                            //send screen image
                            BufferedImage image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ImageIO.write(image, "jpg", byteArrayOutputStream);
                            dataOutputStream.writeInt(byteArrayOutputStream.size());
                            dataOutputStream.write(byteArrayOutputStream.toByteArray());
                            dataOutputStream.flush();
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
