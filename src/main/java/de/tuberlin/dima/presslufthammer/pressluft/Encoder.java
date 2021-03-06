/**
 * 
 */
package de.tuberlin.dima.presslufthammer.pressluft;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * based on / taken from: https://github.com/brunodecarvalho/netty-tutorials
 * 
 * OneToOneEncoder implementation that converts an Envelope instance into a
 * ChannelBuffer.
 * 
 * Since the encoder is stateless, a single instance can be shared among all
 * pipelines, hence the @Sharable annotation and the singleton instantiation.
 */
@ChannelHandler.Sharable
public class Encoder extends OneToOneEncoder {

	// constructors
	// ---------------------------------------------------------------------------------------------------

	private Encoder() {
	}

	// public static methods
	// ------------------------------------------------------------------------------------------

	public static Encoder getInstance() {
		return InstanceHolder.INSTANCE;
	}

	public static ChannelBuffer encodeMessage(Pressluft message)
			throws IllegalArgumentException {
		// you can move these verifications "upper" (before writing to the
		// channel) in order not to cause a
		// channel shutdown

		if ((message.getType() == null) || (message.getType() == Type.UNKNOWN)) {
			throw new IllegalArgumentException(
					"Message type cannot be null or UNKNOWN");
		}

		if ((message.getPayload() == null)
				|| (message.getPayload().length == 0)) {
			throw new IllegalArgumentException(
					"Message payload cannot be null or empty");
		}

		// type(1b) + qid(1b) + payload length(4b) + payload(nb)
		int size = 6 + message.getPayload().length;

		ChannelBuffer buffer = ChannelBuffers.buffer(size);
		buffer.writeByte(message.getType().getByteValue());
		buffer.writeByte(message.getQueryID());
		buffer.writeInt(message.getPayload().length);
		buffer.writeBytes(message.getPayload());

		return buffer;
	}

	// OneToOneEncoder
	// ------------------------------------------------------------------------------------------------

	@Override
	protected Object encode(ChannelHandlerContext channelHandlerContext,
			Channel channel, Object msg) throws Exception {
		if (msg instanceof Pressluft) {
			return encodeMessage((Pressluft) msg);
		} else {
			return msg;
		}
	}

	// private classes
	// ------------------------------------------------------------------------------------------------

	private static final class InstanceHolder {
		private static final Encoder INSTANCE = new Encoder();
	}
}