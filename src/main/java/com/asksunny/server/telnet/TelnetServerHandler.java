package com.asksunny.server.telnet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asksunny.cli.utils.RemoteCommand;
import com.asksunny.cli.utils.RemoteCommandParser;
import com.asksunny.utils.OSUtil;

/**
 * Handles a server-side channel.
 */
@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = Logger.getLogger(TelnetServerHandler.class.getName());
    public static final String EXIT = "exit";
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
        
        	RemoteCommand[] cmds = RemoteCommandParser.parseCommand(request);       
	        for (RemoteCommand cmd : cmds) {
				String[] cmdarray = cmd.getCmdArray();						 
				if(cmdarray[0].equalsIgnoreCase(EXIT)){
					if(future!=null){
						future.addListener(ChannelFutureListener.CLOSE);
					}else{
						ctx.close();
			        	return;
					}
				}
				Runtime rt = Runtime.getRuntime();
				Process p = rt.exec(cmdarray);
				BufferedReader br =new BufferedReader( new InputStreamReader(p.getInputStream() ));
		        String line = null;
		        while((line=br.readLine())!=null){
		        	 future = ctx.write(line+NEWLINE);
		        }
		        br.close();        	
			}        
        }catch(Throwable ex){
        	ctx.write("[ERROR]" + ex.toString()+NEWLINE);
        } 
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.", cause);
        ctx.close();
    }
}