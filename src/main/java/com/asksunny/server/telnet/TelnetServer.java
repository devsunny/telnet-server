package com.asksunny.server.telnet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.LoggerFactory;

import com.asksunny.cli.utils.CLIOptionAnnotationBasedBinder;
import com.asksunny.server.ServerConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * This server is based on netty telnet server example with more detailed implementation.
 */
public class TelnetServer {

    private final ServerConfig config;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TelnetServerHandler.class.getName());
    public static final boolean isWIndows = System.getProperty("os.name").toLowerCase().indexOf("windows")!=-1;
    
    public TelnetServer(ServerConfig config) {
        this.config = config;
        logger.info("{} is starting on {} at port {}.", this.getClass().getName(), System.getProperty("os.name"), config.getPort());
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
            
            if(config.getBindingAddress()==null || config.getBindingAddress().isEmpty()){
            	b.bind(config.getPort()).sync().channel().closeFuture().sync();      
            }else{
            	b.bind(config.getBindingAddress(), config.getPort()).sync().channel().closeFuture().sync();  
            }
            
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
       
    	ServerConfig config = new ServerConfig();
    	Options options = CLIOptionAnnotationBasedBinder.getOptions(config);    	
    	CommandLine cli = CLIOptionAnnotationBasedBinder.bindPosix(options, args, config);    	
    	if(config.getPort()==0){
    		HelpFormatter formatter = new HelpFormatter();
    		formatter.printHelp("TelnetServer", options);
    		System.exit(1);
    	}    	    	
    	new TelnetServer(config).run();
    	
//    	int port;        
//    	if (args.length > 0) {
//            port = Integer.parseInt(args[0]);
//        } else {
//            port = 8080;
//        }
//        
    }
}