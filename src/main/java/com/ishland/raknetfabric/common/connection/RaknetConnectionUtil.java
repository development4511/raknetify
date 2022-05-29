package com.ishland.raknetfabric.common.connection;

import com.ishland.raknetfabric.Constants;
import com.ishland.raknetfabric.common.compat.viafabric.ViaFabricCompatInjector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.server.ServerNetworkIo;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.client.channel.RakNetClientThreadedChannel;
import network.ycc.raknet.frame.Frame;
import network.ycc.raknet.pipeline.ReliabilityHandler;
import network.ycc.raknet.server.channel.RakNetApplicationChannel;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import static com.ishland.raknetfabric.common.connection.SynchronizationLayer.accessible;

public class RaknetConnectionUtil {

    private RaknetConnectionUtil() {
    }

    private static final Comparator<Frame> cmp =
            Comparator
                    .comparingInt((Frame frame) -> frame.getReliability().isReliable ? 1 : 0) // unreliable then reliable
                    .thenComparingInt(frame -> frame.getReliability().isOrdered ? 1 : 0) // unordered then ordered
                    .thenComparingInt(Frame::getOrderChannel) // lower channel first
                    .thenComparingInt(Frame::getOrderIndex); // lower index first

    public static void initChannel(Channel channel) {
        if (channel.config() instanceof RakNet.Config config) {
            config.setMaxQueuedBytes(Constants.MAX_QUEUED_SIZE);
            config.setMaxPendingFrameSets(Constants.MAX_PENDING_FRAME_SETS);
            config.setRetryDelayNanos(TimeUnit.NANOSECONDS.convert(50, TimeUnit.MILLISECONDS));
            config.setDefaultPendingFrameSets(Constants.DEFAULT_PENDING_FRAME_SETS);
            config.setNACKEnabled(false);
//            config.setIgnoreResendGauge(true);

            initRaknetChannel(channel);

//            channel.pipeline().addLast("raknetfabric-flush-enforcer", new FlushEnforcer());
//            channel.pipeline().addLast("raknetfabric-flush-consolidation", new FlushConsolidationHandler(Integer.MAX_VALUE, true));
            channel.pipeline().addLast("raknetfabric-no-flush", new NoFlush());
            channel.pipeline().addLast("raknetfabric-streaming-compression", new MultiChannelingStreamingCompression(Constants.RAKNET_GAME_PACKET_ID, Constants.RAKNET_STREAMING_COMPRESSION_PACKET_ID));
            channel.pipeline().addLast("raknetfabric-multi-channel-data-codec", new MultiChannellingDataCodec(Constants.RAKNET_GAME_PACKET_ID));
            channel.pipeline().addLast("raknetfabric-frame-data-blocker", new FrameDataBlocker());
        }
    }

    private static void initRaknetChannel(Channel appChannel) {
        final Channel channel;
        final String threadedReadHandlerName;
        if (appChannel instanceof RakNetApplicationChannel) {
            channel = appChannel.parent();
            threadedReadHandlerName = RakNetApplicationChannel.NAME_SERVER_PARENT_THREADED_READ_HANDLER;
        } else if (appChannel instanceof RakNetClientThreadedChannel) {
            channel = appChannel.parent();
            threadedReadHandlerName = RakNetClientThreadedChannel.NAME_CLIENT_PARENT_THREADED_READ_HANDLER;
        } else {
            channel = appChannel;
            threadedReadHandlerName = null;
        }
        channel.pipeline().addLast(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                final RakNet.Config config = (RakNet.Config) ch.config();
                final SimpleMetricsLogger simpleMetricsLogger = new SimpleMetricsLogger();
                config.setMetrics(simpleMetricsLogger);
                final MetricsSynchronizationHandler metricsSynchronizationHandler = new MetricsSynchronizationHandler();
                simpleMetricsLogger.setMetricsSynchronizationHandler(metricsSynchronizationHandler);
                final SynchronizationLayer synchronizationLayer = new SynchronizationLayer(Constants.SYNC_IGNORE_CHANNELS);
                reInitChannelForOrdering(channel);
                if (threadedReadHandlerName != null) {
                    ch.pipeline().addBefore(threadedReadHandlerName, "raknetfabric-metrics-sync", metricsSynchronizationHandler);
                    ch.pipeline().addBefore(threadedReadHandlerName, "raknetfabric-synchronization-layer", synchronizationLayer);
                } else {
                    ch.pipeline().addLast("raknetfabric-metrics-sync", metricsSynchronizationHandler);
                    ch.pipeline().addLast("raknetfabric-synchronization-layer", synchronizationLayer);
                }
                ch.pipeline().addFirst("raknetfabric-timeout", new ReadTimeoutHandler(15));
            }
        });
    }

    public static void postInitChannel(Channel channel, boolean isClientSide) {
        if (channel.config() instanceof RakNet.Config) {
            ViaFabricCompatInjector.inject(channel, isClientSide);
            channel.pipeline().replace("timeout", "timeout", new ChannelDuplexHandler()); // no-op
            channel.pipeline().replace("splitter", "splitter", new ChannelDuplexHandler()); // no-op
            channel.pipeline().replace("prepender", "prepender", new ChannelDuplexHandler()); // no-op
            final MultiChannellingPacketCapture handler = new MultiChannellingPacketCapture();
            channel.pipeline().addLast("raknetfabric-multi-channel-packet-cature", handler);
            channel.pipeline().get(MultiChannellingDataCodec.class).setCapture(handler);
        }
    }

    @SuppressWarnings("unchecked")
    private static void reInitChannelForOrdering(Channel channel) {
        if (channel.config() instanceof RakNet.Config config) {
            try {
                final ReliabilityHandler reliabilityHandler = channel.pipeline().get(ReliabilityHandler.class);
                final Field frameQueueField = accessible(ReliabilityHandler.class.getDeclaredField("frameQueue"));
                PriorityQueue<Frame> reliabilityHandlerFrameQueue = (PriorityQueue<Frame>) frameQueueField.get(reliabilityHandler);

                final PriorityQueue<Frame> newSet = new PriorityQueue<>(cmp);
                newSet.addAll(reliabilityHandlerFrameQueue);

                frameQueueField.set(reliabilityHandler, newSet);

            } catch (Throwable t) {
                System.err.println("Error occurred while reinitializing channel ordering");
                t.printStackTrace();
            }
        }
    }

}
