package com.asksunny.server.telnet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * This server is based on netty telnet server example with more detailed implementation.
 */
public class TelnetServer {

    private final int port;

    public TelnetServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
       final  EventLoopGroup bossGroup = new NioEventLoopGroup();
       final  EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
        	TelnetServerInitializer initializer = new TelnetServerInitializer();
        	ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(initializer);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {				
				public void run() {
					bossGroup.shutdownGracefully();
		            workerGroup.shutdownGracefully();					
				}
			}));            
            
            b.bind(port).sync().channel().closeFuture().sync();      
            
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
       
    	
    	
    	int port;        
    	if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new TelnetServer(port).run();
    }
}