package com.skp.ps1.ykh;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	public static final String ROOT_DIRECTORY = "/Users/5001919/Contents";

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
			throws Exception {

		final String uri = request.getUri(); // request의 URI
		final String path = changeURItoAbsolutePath(uri); // URI를 절대경로로 변경
		if (path == null) {
			System.err.println("FORBIDDEN"); // 경로가 없을 땐
			return;
		}

		File file = new File(path);

		if (file.isHidden() || !file.exists()) {
			System.err.println("NOT_FOUND");
			return;
		}

		final RandomAccessFile randomAccessFile;

		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException ignore) {
			return;
		}

		long fileLength = randomAccessFile.length();

		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		HttpHeaders.setContentLength(response, fileLength);

		if (HttpHeaders.isKeepAlive(request)) { // 연결이 아직 되어있을 경우.
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		ctx.write(response);

		ChannelFuture sendFileFuture;
		ChannelFuture lastContentFuture;
		
		sendFileFuture = ctx.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0,fileLength), ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			@Override
			public void operationProgressed(ChannelProgressiveFuture future,long progress, long total) {
				if (total < 0) {
					System.err.println(future.channel()
							+ " Transfer progress: " + progress);
				} else {
					System.err.println(future.channel()
							+ " Transfer progress: " + progress + " / " + total);
				}
			}

			@Override
			public void operationComplete(ChannelProgressiveFuture future) {
				System.err.println(future.channel() + " Transfer complete.");
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if (!HttpHeaders.isKeepAlive(request)) {
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			System.err.println("INTERNAL_SERVER_ERROR");
		}
	}

	private static String changeURItoAbsolutePath(String uri) {
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}

		if (uri.isEmpty() || uri.charAt(0) != '/') {
			return null;
		}

		uri = uri.replace('/', File.separatorChar);

		return ROOT_DIRECTORY + uri;
	}
}
