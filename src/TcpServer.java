import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TcpServer {
    /*
    1. Server Socket - listen and accept connection
    2. Operational Socket (known as a client socket) - read from/write to a data stream
     */

    /*
    Server Outline:
    1. Create a server socket
    2. Bind to a port number
    3. Listens to incoming connections from clients
    4. If a request is accepted, return an operational socket for that specific connection
    5. Handle each client in a separate thread
    5. Extract tasks from InputStream
    6. Tasks are handled according to a concrete strategy
    7. Result is written using socket's OutputStream
    */

    // When we create instance Java initialize FIRST the members by default
    private final int port;
    private boolean stopServer;
    private ThreadPoolExecutor threadPool;
    private IHandler requestHandler; //null by default

    public TcpServer(int port) {
        this.port = port;
        // Boolean value is false by default. good practice:
        this.stopServer = false;
        // threadPool value is null by default. good practice:
        threadPool = null;
    }

    // Listens to incoming requests and handle the clients in separate thread by concrete handler
    // This method running in different thread and NOT in the main thread
    //(The server do another thing in the background and need the MAIN thread
    public void run(IHandler concreteHandler) {
        this.requestHandler = concreteHandler;
        // Creating a new anonymous thread and inject operation to perform.
        // Its very common when we have main operation in the class not store it in variable and then do start.
        new Thread(() -> {
            // We initialize the threadPool here and not in the constructor
            // Because the server can do a lot of thing and until we want begin
            //to listen clients no need to set threadPool in the server.
            threadPool = new ThreadPoolExecutor(5, 10,
                    3, TimeUnit.MICROSECONDS, new LinkedBlockingQueue()); // 5 threads in the array that could increase dynamically depend the loaded.

            try {
                        /*
                        if no port is specified - one will be automatically allocated by OS
                        backlog parameter - number of maximum pending request
                        ServerSocket constructor - socket creation + bit to a specific port
                         */
                ServerSocket serverSocket = new ServerSocket(port, 50);
                while (!stopServer) {

                    // If accept successes Operational Socket is created.
                    Socket serverToSpecificClient = serverSocket.accept(); // 2 operation: listen() + accept(). accept is block calling.
                        /*
                        server will handle each client in a separate thread
                        define every client as a Runnable task to execute the handle logic of the client
                         */
                    Runnable clientHandling = () -> {
                        try {
                            requestHandler.handle(serverToSpecificClient.getInputStream(),
                                    serverToSpecificClient.getOutputStream());
                            // finished handling client. now close all streams:
                            serverToSpecificClient.getInputStream().close();
                            serverToSpecificClient.getOutputStream().close();
                            serverToSpecificClient.close();
                        } catch (IOException ioException) {
                            System.err.println(ioException.getMessage());
                        } catch (ClassNotFoundException ce) {
                            System.err.println(ce.getMessage());
                        }
                    };
                    threadPool.execute(clientHandling);
                }
                serverSocket.close();
                //stopServer = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

    }

    public void stop(){
        if(!stopServer) stopServer = true;
        // execute all the remain tasks in the threadPool and all the running task,then close the threadPool
        if(threadPool!=null) threadPool.shutdown();
    }



}
