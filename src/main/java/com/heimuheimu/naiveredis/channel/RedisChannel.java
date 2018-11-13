/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.naiveredis.channel;

import com.heimuheimu.naivemonitor.facility.MonitoredSocketOutputStream;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.naiveredis.command.Command;
import com.heimuheimu.naiveredis.command.keys.PingCommand;
import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.data.RedisDataReader;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.UnusableServiceNotifier;
import com.heimuheimu.naiveredis.monitor.SocketMonitorFactory;
import com.heimuheimu.naiveredis.net.BuildSocketException;
import com.heimuheimu.naiveredis.net.SocketBuilder;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 与 Redis 服务进行数据交互的管道。协议定义请参考文档：
 * <a href="https://redis.io/topics/protocol">
 * https://redis.io/topics/protocol
 * </a>
 *
 * <h3>Redis 连接信息 Log4j 配置</h3>
 * <blockquote>
 * <pre>
 * log4j.logger.NAIVEREDIS_CONNECTION_LOG=INFO, NAIVEREDIS_CONNECTION_LOG
 * log4j.additivity.NAIVEREDIS_CONNECTION_LOG=false
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG=org.apache.log4j.DailyRollingFileAppender
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.file=${log.output.directory}/naiveredis/connection.log
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.encoding=UTF-8
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.DatePattern=_yyyy-MM-dd
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.layout=org.apache.log4j.PatternLayout
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.layout.ConversionPattern=%d{ISO8601} %-5p : %m%n
 * </pre>
 * </blockquote>
 *
 * <p><strong>说明：</strong>{@code RedisChannel} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class RedisChannel implements Closeable {

    private static final Logger REDIS_CONNECTION_LOG = LoggerFactory.getLogger("NAIVEREDIS_CONNECTION_LOG");

    private static final Logger LOG = LoggerFactory.getLogger(RedisChannel.class);

    /**
     * Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * 与 Redis 服务器建立的 Socket 连接
     */
    private final Socket socket;

    /**
     * PING 命令发送时间间隔，单位：秒
     */
    private final int pingPeriod;

    /**
     * {@code RedisChannel} 不可用通知器，允许为 {@code null}
     */
    private final UnusableServiceNotifier<RedisChannel> unusableServiceNotifier;

    /**
     * 当前数据交互管道使用的 Socket 信息监控器
     */
    private final SocketMonitor socketMonitor;

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    /**
     * 等待发送的 Redis 命令队列
     */
    private final LinkedBlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();

    /**
     * IO 线程
     */
    private RedisIOTask ioTask = null;

    /**
     * 连续 {@link TimeoutException} 异常出现次数
     */
    private volatile long continuousTimeoutExceptionTimes = 0;

    /**
     * 最后一次出现 {@link TimeoutException} 异常的时间戳
     */
    private volatile long lastTimeoutExceptionTime = 0;

    /**
     * 构造一个与 Redis 服务进行数据交互的管道。
     *
     * @param host Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration {@link Socket} 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param unusableServiceNotifier {@code RedisChannel} 不可用通知器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis 地址不符合规则，将会抛出此异常
     * @throws BuildSocketException 如果创建 {@link Socket} 过程中发生错误，将会抛出此异常
     */
    public RedisChannel(String host, SocketConfiguration configuration, int pingPeriod,
                        UnusableServiceNotifier<RedisChannel> unusableServiceNotifier) throws IllegalArgumentException, BuildSocketException {
        this.host = host;
        this.socket = SocketBuilder.create(host, configuration);
        this.pingPeriod = pingPeriod;
        this.unusableServiceNotifier = unusableServiceNotifier;
        this.socketMonitor = SocketMonitorFactory.get(host);
    }

    /**
     * 获得 Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379。
     *
     * @return Redis 地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 判断当前与 Redis 服务进行数据交互的管道是否可用。
     *
     * @return 管道是否可用
     */
    public boolean isAvailable() {
        return state == BeanStatusEnum.NORMAL;
    }

    /**
     * 发送一个 Redis 命令，并返回响应数据。
     *
     * @param command Redis 命令
     * @param timeout 超时时间，单位：毫秒
     * @return 该命令对应的响应数据，不会返回 {@code null}
     * @throws NullPointerException Redis 命令为 {@code null}，将抛出此异常
     * @throws IllegalStateException 当前 {@code RedisChannel} 未初始化或已被关闭，将抛出此异常
     * @throws IllegalStateException Redis 命令在等待响应数据过程中被关闭，将抛出此异常
     * @throws TimeoutException Redis 命令等待响应数据超时，将抛出此异常
     */
    public RedisData send(Command command, long timeout) throws NullPointerException, IllegalStateException, TimeoutException {
        if (command == null) {
            throw new NullPointerException("Send redis command failed: `null command`. Host: `" + host + "`. Timeout: `"
                + timeout + "ms`. State: `" + state + "`. Command: `null`.");
        }
        if (state == BeanStatusEnum.NORMAL) {
            commandQueue.add(command);
        } else {
            throw new IllegalStateException("Send redis command failed: `channel has been closed`. Host: `" + host
                    + "`. Timeout: `" + timeout + "ms`. State: `" + state + "`. Command: `" + command + "`.");
        }
        try {
            return command.getResponseData(timeout);
        } catch (TimeoutException e) {
            //如果两次超时异常发生在 1s 以内，则认为是连续失败
            if (System.currentTimeMillis() - lastTimeoutExceptionTime < 1000) {
                continuousTimeoutExceptionTimes ++;
            } else {
                continuousTimeoutExceptionTimes = 1;
            }
            lastTimeoutExceptionTime = System.currentTimeMillis();
            //如果连续超时异常出现次数大于 50 次，认为当前连接出现异常，关闭当前连接
            if (continuousTimeoutExceptionTimes > 50) {
                REDIS_CONNECTION_LOG.error("RedisChannel need to be closed due to: `too many timeout exceptions[{}]`. Host: `{}`.",
                        continuousTimeoutExceptionTimes, host);
                close();
            }
            throw e;
        }
    }

    /**
     * 执行 {@code RedisChannel} 初始化操作。
     */
    public synchronized void init() {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            try {
                if (socket.isConnected() && !socket.isClosed()) {
                    state = BeanStatusEnum.NORMAL;
                    long startTime = System.currentTimeMillis();
                    SocketConfiguration config = SocketBuilder.getConfig(socket);
                    String socketAddress = host + "/" + socket.getLocalPort();
                    //启动写入线程
                    ioTask = new RedisIOTask(config.getSendBufferSize(), config.getReceiveBufferSize());
                    ioTask.setName("naiveredis-io-" + socketAddress);
                    ioTask.start();
                    REDIS_CONNECTION_LOG.info("RedisChannel has been initialized. Cost: `{}ms`. Host: `{}`. Local port: `{}`. Config: `{}`.",
                            (System.currentTimeMillis() - startTime), host, socket.getLocalPort(), config);
                } else {
                    REDIS_CONNECTION_LOG.error("Initialize RedisChannel failed: `socket is not connected or has been closed`. Host: `{}`.", host);
                    close();
                }
            } catch(Exception e) {
                REDIS_CONNECTION_LOG.error("Initialize RedisChannel failed: `{}`. Host: `{}`.", e.getMessage(), host);
                LOG.error("Initialize RedisChannel failed: `" + e.getMessage() + "`. Host: `" + host + "`. Socket: `" + socket + "`.", e);
                close();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            try {
                //关闭Socket连接
                socket.close();
                //停止IO线程
                if (ioTask != null) {
                    ioTask.stopSignal = true;
                    ioTask.interrupt();
                }
                REDIS_CONNECTION_LOG.info("RedisChannel has been closed. Cost: `{}ms`. Host: `{}`.", (System.currentTimeMillis() - startTime), host);
            } catch (Exception e) {
                REDIS_CONNECTION_LOG.error("Close RedisChannel failed: `{}`. Host: `{}`.", e.getMessage(), host);
                LOG.error("Close RedisChannel failed: `" + e.getMessage() + "`. Host: `" + host + "`. Socket: `"
                        + socket + "`.", e);
            } finally {
                if (unusableServiceNotifier != null) {
                    unusableServiceNotifier.onClosed(this);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "RedisChannel{" +
                "host='" + host + '\'' +
                ", socket=" + socket +
                ", pingPeriod=" + pingPeriod +
                ", state=" + state +
                ", continuousTimeoutExceptionTimes=" + continuousTimeoutExceptionTimes +
                ", lastTimeoutExceptionTime=" + lastTimeoutExceptionTime +
                '}';
    }

    private class RedisIOTask extends Thread {

        private final MonitoredSocketOutputStream outputStream;

        private final int sendBufferSize;

        private final byte[] sendBuffer;

        private final RedisDataReader reader;

        private volatile boolean stopSignal = false;

        private int sendBufferOffset = 0;

        /**
         * 合并的 Redis 命令列表
         */
        private final ArrayList<Command> mergedCommandList = new ArrayList<>();

        /**
         * 等待响应数据的 Redis 命令队列
         */
        private final LinkedList<Command> waitingQueue = new LinkedList<>();

        private RedisIOTask(Integer sendBufferSize, Integer receiveBufferSize) throws IOException {
            this.outputStream = new MonitoredSocketOutputStream(socket.getOutputStream(), socketMonitor);

            this.sendBufferSize = sendBufferSize != null ? sendBufferSize : 64 * 1024;
            this.sendBuffer = new byte[sendBufferSize];

            receiveBufferSize = receiveBufferSize != null ? receiveBufferSize : 64 * 1024;
            this.reader = new RedisDataReader(socketMonitor, socket.getInputStream(), receiveBufferSize);
        }

        @Override
        public void run() {
            Command command;
            while (!stopSignal) {
                try {
                    if (pingPeriod <= 0) {
                        command = commandQueue.take();
                    } else {
                        command = commandQueue.poll(pingPeriod, TimeUnit.SECONDS);
                        if (command == null) { // 如果心跳检测时间内没有请求，创建一个 Ping 命令进行发送
                            final PingCommand pingCommand = new PingCommand();
                            new Thread() { // 启动一个异步线程检查心跳是否有正常返回

                                @Override
                                public void run() {
                                    try {
                                        RedisData responseData = pingCommand.getResponseData(5000);
                                        if (!responseData.isSimpleString() || !"PONG".equals(responseData.getText())) { // PING 命令非预期返回
                                            REDIS_CONNECTION_LOG.info("RedisChannel need to be closed: `invalid response for PING command`. Host: `{}`. Timeout: `5000ms`. Response: `{}`.", host, responseData);
                                            RedisChannel.this.close();
                                        } else {
                                            LOG.debug("Receive response for PING command success. Host: `{}`.", host);
                                        }
                                    } catch (TimeoutException e) {
                                        REDIS_CONNECTION_LOG.info("RedisChannel need to be closed: `wait response timeout for PING command`. Host: `{}`. Timeout: `5000ms`.", host);
                                        RedisChannel.this.close();
                                    } catch (Exception e) {
                                        REDIS_CONNECTION_LOG.info("RedisChannel need to be closed: `unexpected error for PING command. See the naiveredis log for more information. `. Host: `{}`. Timeout: `5000ms`.", host);
                                        LOG.error("RedisChannel need to be closed: `unexpected error for PING command`. Host: `" + host + "`. Timeout: `5000ms`.", e);
                                        RedisChannel.this.close();
                                    }
                                }

                            }.start();
                            command = pingCommand;
                        }
                    }

                    byte[] requestPacket = command.getRequestByteArray();
                    if (requestPacket.length >= sendBufferSize) {
                        sendMergedPacket();
                        sendPacket(command, requestPacket);
                        outputStream.flush();
                    } else {
                        boolean isMerged = addToMergedPacket(command, requestPacket);
                        if (!isMerged) { // 发送缓存区大小不足，发送后再进行合并
                            sendMergedPacket();
                            outputStream.flush();
                            if (!addToMergedPacket(command, requestPacket)) { //never happen,just for bug detection
                                throw new IllegalStateException("Merge command failed: `not enough buffer size, must be NaiveRedis's bug.`.");
                            }
                        }
                        if (commandQueue.size() == 0) {
                            sendMergedPacket();
                            outputStream.flush();
                        }
                    }

                    //如果该连接某个命令一直等待不到返回，可能会一直阻塞
                    while (waitingQueue.size() > 0) {
                        command = waitingQueue.peek();
                        RedisData responseData = reader.read();
                        if (responseData != null) {
                            command.receiveResponseData(responseData);
                            waitingQueue.poll();
                        } else {
                            REDIS_CONNECTION_LOG.info("RedisChannel need to be closed: `end of the input stream has been reached`. Host: `{}`.", host);
                            close();
                            break;
                        }
                    }
                } catch (InterruptedException ignored) { //do nothing

                } catch (IOException e) {
                    REDIS_CONNECTION_LOG.error("RedisChannel need to be closed: `[IOException] {}`. Host: `{}`.", e.getMessage(), host);
                    LOG.error("RedisChannel need to be closed: `[IOException] " + e.getMessage() + "`. Host: `" + host
                            + "`. Socket: `" + socket + "`.", e);
                    close();
                } catch (Exception e) {
                    REDIS_CONNECTION_LOG.error("RedisChannel need to be closed: `{}`. Host: `{}`.", e.getMessage(), host);
                    LOG.error("RedisChannel need to be closed: `" + e.getMessage() + "`. Host: `" + host
                            + "`. Socket: `" + socket + "`.", e);
                    close();
                }
            }
            for (Command waitingCommand : waitingQueue) {
                waitingCommand.close();
            }
            for (Command mergedCommand : mergedCommandList) {
                mergedCommand.close();
            }
            Command commandInQueue;
            while ((commandInQueue = RedisChannel.this.commandQueue.poll()) != null) {
                commandInQueue.close();
            }
        }

        private boolean addToMergedPacket(Command command, byte[] requestPacket) {
            if (sendBufferOffset + (requestPacket.length - 1) < sendBufferSize) {
                System.arraycopy(requestPacket, 0, sendBuffer, sendBufferOffset, requestPacket.length);
                sendBufferOffset += requestPacket.length;

                mergedCommandList.add(command);
                return true;
            } else {
                return false;
            }
        }

        private void sendMergedPacket() throws IOException {
            if (sendBufferOffset > 0) {
                outputStream.write(sendBuffer, 0, sendBufferOffset);

                waitingQueue.addAll(mergedCommandList);
                mergedCommandList.clear();
                sendBufferOffset = 0;
            }
        }

        private void sendPacket(Command command, byte[] requestPacket) throws IOException {
            outputStream.write(requestPacket);

            waitingQueue.add(command);
        }
    }
}
