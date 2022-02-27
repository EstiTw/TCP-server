import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IHandler {
    /*
    The tasks will be performed using a concrete handler
    the concrete handler will meet the contract written in this interface.

    This interface knows how to get a sequence of data according to the type of task
    and it has the functionality that the concrete handlers need.
     */
    public abstract void handle(InputStream fromClient, OutputStream toClient) throws IOException, ClassNotFoundException;


}
