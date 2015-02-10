package com.skp.ps1.ykh;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public class ServerMain {
	
static protected int PORT = 8080;
	
	public static void main(String args[]) throws Exception{
		
		EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup();
		EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
		
		try{
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossEventLoopGroup,workerEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.TRACE))
			.childHandler(new ServerInitializer());
			Channel httpChannel = serverBootstrap.bind(PORT).sync().channel();
			
			System.out.println("http://localhost:"+PORT+"/ 에 접속하세요.");
			
			httpChannel.closeFuture().sync();
		}finally{
			bossEventLoopGroup.shutdownGracefully();
			workerEventLoopGroup.shutdownGracefully();
		}
	}
}
