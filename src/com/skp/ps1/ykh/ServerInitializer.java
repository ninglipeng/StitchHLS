package com.skp.ps1.ykh;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;


public class ServerInitializer extends ChannelInitializer<SocketChannel>{

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		// TODO Auto-generated method stub
		ChannelPipeline pipeline = arg0.pipeline();
		
		/**
		 * 서버 쪽 HTTP 구현을 하기 쉽게 하기 위해 HttpRequestDecoder와 HttpResponseEncoder를 묶어 놓은 채널핸들러
		 */
		pipeline.addLast(new HttpServerCodec());
		
		/**
		 * HttpMessage와 이를 따르는 HttpContents를 request/response 중 핸들링하는 것에 따라 하나의 FullHttpRequest나 FullHttpResponse로 모아놓은 채널핸들러이다.
		 * HTTP 메시지를 전송하기위해 엔코딩하는 것을 신경쓰지않고싶은 사람에게 유용한 핸들러이다.
		 */
		pipeline.addLast(new HttpObjectAggregator(65536)); 	

		/**
		 * OutOfMemoryError를 얻거나 많은 메모리를 소모하지 않으면서 비동기로 큰 데이터를 쓰기 위해 추가하는 채널 핸들러이다.
		 * 파일전송과 같은 큰 데이터의 스트리밍은 복잡한 상태(state)관리가 필요한데 그걸 이 채널핸들러가 다 해준다.
		 */
		pipeline.addLast(new ChunkedWriteHandler());		
		
		/**
		 * HTTP서버를 핸들링하고 request를 처리해서 response를 클라이언트에게 보내기 위해 생성한 채널핸들러이다.
		 */
		pipeline.addLast(new ServerHandler());
	}
}