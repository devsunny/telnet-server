package com.asksunny.server.telnet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.asksunny.cli.utils.RemoteCommand;
import com.asksunny.cli.utils.RemoteCommandParser;
import com.asksunny.utils.OSUtil;

/**
 * Handles a server-side channel.
 */
@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TelnetServerHandler.class.getName());
    public static final String EXIT = "exit";
    public static final String BYE = "bye";
    public static final String QUIT = "quit";
    
    public static final String SHUTDOWN = "shutdown";
    public static final String NEWLINE = OSUtil.OsLineSeparator();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, String request) throws Exception {
       
        if (request.isEmpty()) {
           return;
        }         
        if(request.equalsIgnoreCase(EXIT)){
        	ctx.close();        	
        	return;
        }
        
        ChannelFuture future = null;
        try{
        	if(logger.isDebugEnabled()) logger.debug("Recieved Request:{}", request);
        	RemoteCommand[] cmds = RemoteCommandParser.parseCommand(request);       
	        for (RemoteCommand cmd : cmds) {
				String[] cmdarray = cmd.getCmdArray();						 
				if(cmdarray[0].equalsIgnoreCase(EXIT) || cmdarray[0].equalsIgnoreCase(BYE) || cmdarray[0].equalsIgnoreCase(QUIT) ){
					if(future!=null){
						future.addListener(ChannelFutureListener.CLOSE);
					}else{
						ctx.close();
			        	return;
					}
				}else if(cmdarray[0].equalsIgnoreCase(SHUTDOWN)  ){
					if(future!=null){
						future.addListener(ChannelFutureListener.CLOSE);
					}else{
						ctx.close();			        	
					}
					Runtime.getRuntime().exit(0);	
					return;
				}
				ProcessBuilder builder = null;				
				if(TelnetServer.isWIndows){
					List<String> cmdss = new ArrayList<String>(Arrays.asList(cmdarray));
					cmdss.add(0, "cmd.exe");
					cmdss.add(1, "/C");
					builder  = new ProcessBuilder(cmdss);
					if(logger.isDebugEnabled()) logger.debug("Cmd processed:{}", cmdss);
				}else{
					List<String> cmdss = Arrays.asList(cmdarray);
					builder  = new ProcessBuilder(cmdss);
					if(logger.isDebugEnabled()) logger.debug("Cmd processed:{}", cmdss);
				}
				builder.environment().putAll(System.getenv());
				builder.redirectErrorStream(true);
				Process p = builder.start();				
				BufferedReader br =new BufferedReader( new InputStreamReader(p.getInputStream() ));
		        String line = null;
		        while((line=br.readLine())!=null){
		        	 future = ctx.write(line+NEWLINE);
		        }
		        br.close();		        
		        p.waitFor();
			}        
        }catch(Throwable ex){
        	ctx.write("[ERROR]" + ex.toString()+NEWLINE);
        	logger.warn("Failed to process command.", ex);
        } 
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {       
        ctx.close();
    }
}