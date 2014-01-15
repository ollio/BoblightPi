package org.bozan.boblight.input;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.bozan.boblight.configuration.BoblightConfiguration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoblightServerNetty implements BoblightServer {

  private static final Logger LOG = Logger.getLogger(BoblightServerNetty.class.getName());
  private static final int MAX_LENGTH = 8192;

  private static final StringDecoder DECODER = new StringDecoder();
  private static final StringEncoder ENCODER = new StringEncoder();

  private final int port;
  private final BoblightProtocolHandler protocolHandler;

  public BoblightServerNetty() throws IOException {
    protocolHandler = new BoblightProtocolHandlerImpl();
    port = BoblightConfiguration.getInstance().getPort();
  }

  @Override
  public void run() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup);
      b.channel(NioServerSocketChannel.class);
      b.option(ChannelOption.SO_BACKLOG, 100);
      b.childHandler(new SocketChannelChannelInitializer());

      // Start the server.
      ChannelFuture f = b.bind(port).sync();

      // Wait until the server socket is closed.
      f.channel().closeFuture().sync();
    } catch (InterruptedException ex) {
      LOG.severe("Netty server interrupted: " + ex.getMessage());
    } finally {
      // Shut down all event loops to terminate all threads.
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  private class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();

      // Add the text line codec combination first,
      pipeline.addLast("framer", new DelimiterBasedFrameDecoder(MAX_LENGTH, Delimiters.lineDelimiter()));
      // the encoder and decoder are static as these are sharable
      pipeline.addLast("decoder", DECODER);
      pipeline.addLast("encoder", ENCODER);

      // and then business logic.
      pipeline.addLast("handler", new BoblightServerHandler());    }
  }

  private class BoblightServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, String msg) throws Exception {
      protocolHandler.handleMessage(msg, new BoblightProtocolHandler.ResponseHandler() {
        @Override
        public void onResponse(String response) {
          ctx.write(response + "\r\n");
          ctx.flush();
        }
      });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      // Close the connection when an exception is raised.
      LOG.log(Level.WARNING, "Unexpected exception from downstream.", cause);
      ctx.close();
    }
  }
}
