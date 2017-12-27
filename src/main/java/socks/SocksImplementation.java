package socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

public class SocksImplementation extends SocketImpl{

    public static void main(String[] args) {

    }

    @Override
    protected void create(boolean b) throws IOException {

    }

    @Override
    protected void connect(String s, int i) throws IOException {

    }

    @Override
    protected void connect(InetAddress inetAddress, int i) throws IOException {

    }

    @Override
    protected void connect(SocketAddress socketAddress, int i) throws IOException {

    }

    @Override
    protected void bind(InetAddress inetAddress, int i) throws IOException {

    }

    @Override
    protected void listen(int i) throws IOException {

    }

    @Override
    protected void accept(SocketImpl socket) throws IOException {

    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    protected int available() throws IOException {
        return 0;
    }

    @Override
    protected void close() throws IOException {

    }

    @Override
    protected void sendUrgentData(int i) throws IOException {

    }

    @Override
    public void setOption(int i, Object o) throws SocketException {

    }

    @Override
    public Object getOption(int i) throws SocketException {
        return null;
    }
}
