package me.songt.nettychat.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ChatServer
{
    private int serverPort = 8009;
    private Logger logger = LoggerFactory.getLogger(ChatServer.class);

    public static final int READ_WAIT_SECONDS = 6;
    public static final int MAX_UNREPLY_COUNT = 4;


    public ChatServer(int serverPort)
    {
        this.serverPort = serverPort;
    }

    public ChatServer()
    {
    }

    public void start() throws InterruptedException
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try
        {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(serverPort))
                    .childHandler(new ChatServerInitializer());
            ChannelFuture future = bootstrap.bind(serverPort).sync();
            logger.info("Server started. Listen: " + future.channel().localAddress());
            future.channel().closeFuture().sync();
        } finally
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
